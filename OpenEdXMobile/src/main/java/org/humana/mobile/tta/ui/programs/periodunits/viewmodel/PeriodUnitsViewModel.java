package org.humana.mobile.tta.ui.programs.periodunits.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableLong;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.lib.mxcalendar.models.Event;
import com.lib.mxcalendar.view.IMxCalenderListener;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowFilterDropDownBinding;
import org.humana.mobile.databinding.TRowUnitBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.ShowIn;
import org.humana.mobile.tta.data.enums.UnitStatusType;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.SelectedFilter;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.event.program.ProgramFilterSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.tta.ui.programs.addunits.AddUnitsActivity;
import org.humana.mobile.tta.ui.programs.units.FragmentCalendarBottomSheet;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.DateUtil;
import org.humana.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class PeriodUnitsViewModel extends BaseViewModel implements IMxCalenderListener {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean addUnitsVisible = new ObservableBoolean();
    public ObservableField<String> periodName = new ObservableField<>();
    private boolean isfilterSelected;
    private boolean isChangefilter;


    private EnrolledCoursesResponse course;
    private long periodId;
    private List<Unit> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> filters;
    private List<ProgramFilter> allFilters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private EnrolledCoursesResponse parentCourse;
    public ObservableField<String> searchText = new ObservableField<>("");
    private List<SelectedFilter> selectedFilter;

//    Calendar View

    public ObservableField<List<Event>> eventObservable = new ObservableField<>();
    public static List<Event> eventsArrayList = new ArrayList<>();
    public static long startDateTime, endDateTime;
    public ObservableBoolean calVisible = new ObservableBoolean();
    public ObservableBoolean frameVisible = new ObservableBoolean();
    public ObservableBoolean isCheckedObserver = new ObservableBoolean();
    public ObservableLong eventObservableDate = new ObservableLong();


    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public PeriodUnitsViewModel(BaseVMActivity activity, long periodId, String periodName, EnrolledCoursesResponse course) {
        super(activity);
        emptyVisible.set(false);
        this.course = course;
        this.periodId = periodId;
        this.periodName.set(periodName);
        units = new ArrayList<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();

//        filters = Constants.PROG_FILTER;
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;

        unitsAdapter = new UnitsAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
        selectedFilter = new ArrayList<>();
        selectedFilter = mDataManager.getSelectedFilters();
        isfilterSelected = false;
        isChangefilter = true;

        unitsAdapter.setItems(units);
        unitsAdapter.setItemClickListener((view, item) -> {

            switch (view.getId()) {
                case R.id.tv_my_date:
                    String title;
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        title = mActivity.getString(R.string.proposed_date);
                    } else {
                        title = mActivity.getString(R.string.my_date);
                    }
                    showDatePicker(item, title);
                    break;
                default:
                    if (item.isPublish()) {
                        mActivity.showLoading();
                        boolean ssp = units.contains(item);
                        EnrolledCoursesResponse c;
                        if (ssp) {
                            c = course;
                        } else {
                            c = parentCourse;
                        }

                        if (c == null) {

                            String courseId;
                            if (ssp) {
                                courseId = mDataManager.getLoginPrefs().getProgramId();
                            } else {
                                courseId = mDataManager.getLoginPrefs().getParentId();
                            }
                            mDataManager.enrolInCourse(courseId, new OnResponseCallback<ResponseBody>() {
                                @Override
                                public void onSuccess(ResponseBody responseBody) {

                                    mDataManager.getenrolledCourseByOrg("Humana", new OnResponseCallback<List<EnrolledCoursesResponse>>() {
                                        @Override
                                        public void onSuccess(List<EnrolledCoursesResponse> data) {
                                            if (courseId != null) {
                                                for (EnrolledCoursesResponse response : data) {
                                                    if (response.getCourse().getId().trim().toLowerCase()
                                                            .equals(courseId.trim().toLowerCase())) {
                                                        if (ssp) {
                                                            PeriodUnitsViewModel.this.course = response;
                                                            EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                        } else {
                                                            PeriodUnitsViewModel.this.parentCourse = response;
                                                        }
                                                        getBlockComponent(item);
                                                        break;
                                                    }
                                                }
                                                mActivity.hideLoading();
                                            } else {
                                                mActivity.hideLoading();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            mActivity.hideLoading();
                                            mActivity.showLongSnack("enroll org failure");
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    mActivity.hideLoading();
                                    mActivity.showLongSnack("enroll failure");
                                }
                            });

                        } else {
                            getBlockComponent(item);
                        }

                    } else {
                        mActivity.showShortSnack(mActivity.getString(R.string.unit_not_published));
                    }
            }

        });

        if (emptyVisible.get()) {
            addUnitsVisible.set(true);
        } else {
            addUnitsVisible.set(false);
        }

        mActivity.showLoading();
        fetchFilters();
    }

    private void getBlockComponent(Unit unit) {

        mDataManager.getBlockComponent(unit.getId(), mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<CourseComponent>() {
                    @Override
                    public void onSuccess(CourseComponent data) {
                        mActivity.hideLoading();

                        if (PeriodUnitsViewModel.this.course == null) {
                            mActivity.showLongSnack("You're not enrolled in the program");
                            return;
                        }

                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                    PeriodUnitsViewModel.this.course, data.getChildren().get(0).getId(),
                                    null, false);
                        } else {
                            mActivity.showLongSnack("This unit is empty");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void showDatePicker(Unit unit, String title) {
        DateUtil.showDatePicker(mActivity, unit.getMyDate(), title, new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                mActivity.showLoading();
                mDataManager.setProposedDate(mDataManager.getLoginPrefs().getProgramId(),
                        mDataManager.getLoginPrefs().getSectionId(), data, unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
                                unit.setMyDate(data);
                                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));
                                mActivity.showLongSnack("Proposed date set successfully");

                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack(e.getLocalizedMessage());
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void fetchFilters() {
        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.periodunits.name(), filters,
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {

                       if (periodId!=0) {
                           for (ProgramFilter filter : data) {
                               if (filter.getInternalName().toLowerCase().contains("period_id")) {
                                   for (ProgramFilterTag tag : filter.getTags()) {
                                       if (tag.getId() == periodId) {
                                           SelectedFilter sf = new SelectedFilter();
                                           sf.setInternal_name(filter.getInternalName());
                                           sf.setDisplay_name(filter.getDisplayName());
                                           sf.setSelected_tag(tag.getDisplayName());
                                           sf.setSelected_tag_item(tag);
                                           mDataManager.updateSelectedFilters(sf);
                                           break;
                                       }
                                   }
                                   break;
                               }
                           }
                           selectedFilter = mDataManager.getSelectedFilters();
                       }

                        if (!data.isEmpty()) {
                            allFilters = data;
                            filtersVisible.set(true);
                            filtersAdapter.setItems(data);
                        } else {
                            filtersVisible.set(false);
                        }
                        changesMade = true;
                        fetchData();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        filtersVisible.set(false);
                        fetchData();
                    }
                });

    }

    public void fetchData() {

        if (changesMade) {
            changesMade = false;
            skip = 0;
            unitsAdapter.reset(true);
            setUnitFilters();
        }

        if (isCheckedObserver.get()) {
            fetchUnits(startDateTime, endDateTime);
            calVisible.set(true);
            frameVisible.set(false);
        } else {
            calVisible.set(false);
            frameVisible.set(true);
            fetchUnits();
        }

    }

    private void setUnitFilters() {
        if (filters != null)
            filters.clear();
        if (allFilters == null || allFilters.isEmpty()) {
            return;
        }
        if (selectedFilter.isEmpty()) {
            for (ProgramFilter filter : allFilters) {
                for (ProgramFilterTag tag : filter.getTags()) {
                    if (tag.getSelected()) {
                        SelectedFilter sf = new SelectedFilter();
                        sf.setInternal_name(filter.getInternalName());
                        sf.setDisplay_name(filter.getDisplayName());
                        sf.setSelected_tag(tag.getDisplayName());
                        sf.setSelected_tag_item(tag);
                        mDataManager.updateSelectedFilters(sf);
                        selectedFilter.addAll(mDataManager.getSelectedFilters());
                        break;
                    }
                }
            }


        }
        for (SelectedFilter selected : selectedFilter) {
            for (ProgramFilter filter : allFilters) {
                List<ProgramFilterTag> selectedTags = new ArrayList<>();
                if (selected.getInternal_name().equalsIgnoreCase(filter.getInternalName())) {
                    for (ProgramFilterTag tag : filter.getTags()) {
                        if (selected.getSelected_tag() != null) {
                            if (selected.getSelected_tag_item()!=null) {
                                if (selected.getSelected_tag_item().equals(tag)) {
                                    selectedTags.add(tag);
                                    ProgramFilter pf = new ProgramFilter();
                                    pf.setDisplayName(filter.getDisplayName());
                                    pf.setInternalName(filter.getInternalName());
                                    pf.setId(filter.getId());
                                    pf.setOrder(filter.getOrder());
                                    pf.setShowIn(filter.getShowIn());
                                    pf.setTags(selectedTags);
                                    if (!filters.contains(pf)) {
                                        filters.add(pf);
                                    }

                                    break;
                                }
                            }else {
                                if (selected.getSelected_tag().equals(tag.getDisplayName())) {
                                    selectedTags.add(tag);
                                    ProgramFilter pf = new ProgramFilter();
                                    pf.setDisplayName(filter.getDisplayName());
                                    pf.setInternalName(filter.getInternalName());
                                    pf.setId(filter.getId());
                                    pf.setOrder(filter.getOrder());
                                    pf.setShowIn(filter.getShowIn());
                                    pf.setTags(selectedTags);
                                    if (!filters.contains(pf)) {
                                        filters.add(pf);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isChangefilter){
            fetchFilters();
            isChangefilter = false;
        }

    }

    private void fetchUnits() {
        mActivity.showLoading();
        mDataManager.getUnits(filters, searchText.get(), mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole(), "",
                periodId, take, skip, 0L, 0L,
                new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }

                        populateUnits(data);
                        unitsAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        unitsAdapter.setLoadingDone();
//                        unitsAdapter.reset(true);
                        toggleEmptyVisibility();

                    }
                });

    }


    public void fetchUnits(Long startDate, Long endDate) {
        mActivity.showLoading();
        mDataManager.getUnits(filters, searchText.get(), mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole(),
                "", 0L, 0, 0, startDate, endDate,
                new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        populateUnits(data);
                        unitsAdapter.setLoadingDone();
                        mActivity.hideLoading();

                        if (data.size() == 0) {
                            emptyVisible.set(true);
                            calVisible.set(false);
                            frameVisible.set(false);
                        } else {
                            emptyVisible.set(false);
                            calVisible.set(true);
                            frameVisible.set(false);
                        }

                        eventsArrayList.clear();
                        String colorCode = "#ffffff";
                        Event et;
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getCommonDate() > 0) {
                                    switch (data.get(i).getType()) {
                                        case "Study Task":
                                            colorCode = "#F8E56B";
                                            et = new Event(DateUtil.getDisplayDate(data.get(i).getCommonDate()),
                                                    data.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Experience":
                                            colorCode = "#33FFAC";
                                            et = new Event(DateUtil.getDisplayDate(data.get(i).getCommonDate()),
                                                    data.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Course":
                                            colorCode = "#EF98FC";
                                            et = new Event(DateUtil.getDisplayDate(data.get(i).getCommonDate()),
                                                    data.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                    }

                                }
                            }
                        } else {

                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getMyDate() > 0) {
                                    switch (data.get(i).getType()) {
                                        case "Study Task":
                                            colorCode = "#F8E56B";
                                            et = new Event(DateUtil.getDisplayDate(data.get(i).getMyDate()),
                                                    data.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Experience":
                                            colorCode = "#33FFAC";
                                            et = new Event(DateUtil.getDisplayDate(data.get(i).getMyDate()),
                                                    data.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Course":
                                            colorCode = "#EF98FC";
                                            et = new Event(DateUtil.getDisplayDate(data.get(i).getMyDate()),
                                                    data.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                    }

                                }
                            }
                        }
                        startDateTime = startDate;
                        endDateTime = endDate;
                        eventObservableDate.set(startDate);
                        eventObservable.set(eventsArrayList);
                        eventObservable.notifyChange();
                        mActivity.hideLoading();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        List<Event> events = new ArrayList<>();
                        allLoaded = true;
                        unitsAdapter.setLoadingDone();
//                        toggleEmptyVisibility();
                        mActivity.hideLoading();
                        Event et;
                        for (Event event : eventsArrayList) {
                            et = new Event(event.getDATE(),
                                    "", null, "#ffffff");
                            events.add(et);
                        }
                        eventObservable.set(events);
                        eventObservable.notifyChange();
                    }
                });
    }

    private void populateUnits(List<Unit> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Unit unit : data) {
            if (!unitAlreadyAdded(unit)) {
                units.add(unit);
                newItemsAdded = true;
                n++;
            }
        }

        if (newItemsAdded) {
            unitsAdapter.notifyItemRangeInserted(units.size() - n, n);
        }

        toggleEmptyVisibility();
    }
    private boolean unitAlreadyAdded(Unit unit) {
        for (Unit u : units) {
            if (TextUtils.equals(u.getId(), unit.getId()) && (u.getPeriodId() == unit.getPeriodId())) {
                return true;
            }
        }
        return false;
    }

    private void toggleEmptyVisibility() {
        mActivity.hideLoading();
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
            addUnitsVisible.set(true);
        } else {
            emptyVisible.set(false);
            addUnitsVisible.set(false);
        }
    }

    public void addUnits() {
        if (periodId!=0) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_PERIOD_NAME, periodName.get());
            parameters.putLong(Constants.KEY_PERIOD_ID, periodId);
            parameters.putSerializable(Router.EXTRA_COURSE_DATA, course);
            ActivityUtil.gotoPage(mActivity, AddUnitsActivity.class, parameters);
        }else {
            mActivity.showShortToast(mActivity.getString(R.string.select_period));
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PeriodSavedEvent event) {
        if (event.getPeriodId() != periodId) {
            return;
        }
        changesMade = true;
        allLoaded = false;
        mActivity.showLoading();
        fetchData();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(List<Unit> unit) {
        changesMade = true;
        allLoaded = false;
        fetchData();
    }

    public void onEventMainThread(ProgramFilterSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        filters = event.getProgramFilters();
//        fetchFilters();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(CourseEnrolledEvent event) {
        this.course = event.getCourse();
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAction(long date, long startDateTime, long endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        fetchData();
    }

    @Override
    public void onItemClick(Long selectedDate, Long startDateTime, Long endDateTime) {
        FragmentCalendarBottomSheet bottomSheetDialogFragment =
                new FragmentCalendarBottomSheet(selectedDate, startDateTime, endDateTime, periodId
                        , periodName.get(), filters);
        bottomSheetDialogFragment.show(mActivity.getSupportFragmentManager(),
                "PeriodUnits");
    }

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model, @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof TRowFilterDropDownBinding) {
                TRowFilterDropDownBinding dropDownBinding = (TRowFilterDropDownBinding) binding;

                List<DropDownFilterView.FilterItem> items = new ArrayList<>();
                String selectedTag = "";
                items.add(new DropDownFilterView.FilterItem(model.getDisplayName(), null,
                        true, R.color.primary_cyan, R.drawable.t_background_tag_hollow
                ));

                for (ProgramFilterTag tag : model.getTags()) {
                    items.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                            tag.getSelected(), R.color.white, R.drawable.t_background_tag_filled
                    ));

                }


                dropDownBinding.filterDropDown.setFilterItems(items);

                if (selectedFilter != null) {
                    for (ProgramFilterTag tag : model.getTags()) {
                        for (SelectedFilter item : selectedFilter) {
                            if (item.getInternal_name().equalsIgnoreCase(model.getInternalName())) {
                                if (item.getSelected_tag_item()!=null) {
                                    if (item.getSelected_tag_item().equals(tag)) {
                                        dropDownBinding.filterDropDown.setSelection(item.getSelected_tag_item());
                                    }
                                    break;
                                }else {
                                    if (item.getSelected_tag().equals(model.getDisplayName())) {
                                        dropDownBinding.filterDropDown.setSelection(item.getDisplay_name());
                                    }

                                }
                            }
                        }
                    }
                }
                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {

                    if (item.getItem() != null) {
                        ProgramFilterTag tag = (ProgramFilterTag) item.getItem();
                        if (model.getInternalName().toLowerCase().contains("period_id")) {
                            periodName.set(tag.getDisplayName());
                            periodId = tag.getId();
                        }
                    }else if (item.getName().equalsIgnoreCase("Period")){
                        periodName.set("Period");
                        periodId = 0;
                    }
                    SelectedFilter sf = new SelectedFilter();
                    sf.setInternal_name(model.getInternalName());
                    sf.setDisplay_name(model.getDisplayName());
                    sf.setSelected_tag(item.getName());
                    sf.setSelected_tag_item((ProgramFilterTag) item.getItem());
                    mDataManager.updateSelectedFilters(sf);

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    selectedFilter = mDataManager.getSelectedFilters();
                    filters.clear();
                    for (SelectedFilter selected : selectedFilter) {
                        for (ProgramFilter filter : allFilters) {
                            List<ProgramFilterTag> selectedTags = new ArrayList<>();
                            if (selected.getInternal_name().equalsIgnoreCase(filter.getInternalName())) {
                                for (ProgramFilterTag tag : filter.getTags()) {
                                    if (selected.getSelected_tag() != null) {
                                        if (selected.getSelected_tag_item()!=null) {
                                            if (selected.getSelected_tag_item().equals(tag)) {
                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                break;
                                            }
                                        }else {
                                            if (selected.getSelected_tag().equals(tag.getDisplayName())) {
                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                break;
                                            } else if (selected.getSelected_tag().equals(filter.getDisplayName())) {
//                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                isfilterSelected = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

//                    for (ProgramFilter filter: filters) {
//                        if (!filter.getInternalName().toLowerCase().equalsIgnoreCase("period_id")) {
//                            periodId = 0;
//                        }
//                    }
                    EventBus.getDefault()
                            .post(new ProgramFilterSavedEvent(filters, true));
                    fetchFilters();

                });
            }
        }
    }

    public class UnitsAdapter extends MxInfiniteAdapter<Unit> {
        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model,
                           @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowUnitBinding) {
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);

                unitBinding.unitCode.setText(model.getTitle());
                unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                        + model.getUnitHour() + " " + mActivity.getResources().getString(R.string.point_txt));
                if (!model.getStatus().equals("")) {
                    if (model.getStatusDate() > 0) {
                        if (!DateUtil.getDisplayDate(model.getStatusDate()).equals("01 Jan 1970")) {
                            unitBinding.tvStaffDate.setText(model.getStatus() + ": " + DateUtil.getDisplayDate(model.getStatusDate()));
                            unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                }
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                    if (model.getStaffDate() > 0) {
                        if (!DateUtil.getDisplayDate(model.getStaffDate()).equals("01 Jan 1970")) {
                            unitBinding.tvSubmittedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                            unitBinding.tvSubmittedDate.setVisibility(View.VISIBLE);
                        } else {
                            unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                        }
                    }
                } else {
                    unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                }
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                    if (!model.getStatus().equals("")) {
                        unitBinding.tvComment.setText(model.getStatus() + " comments : " + model.getComment());
                        if (model.getStatus().equals("Submitted")) {
                            unitBinding.tvComment.setVisibility(View.GONE);
                        } else {
                            unitBinding.tvComment.setVisibility(View.VISIBLE);
                        }
                    } else {
                        unitBinding.tvComment.setVisibility(View.GONE);
                    }
                } else {
                    unitBinding.tvComment.setVisibility(View.GONE);
                }
                unitBinding.tvDescription.setText(model.getDesc());
                unitBinding.checkbox.setVisibility(View.GONE);

                if (model.getMyDate() > 0) {
                    unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                } else {
                    unitBinding.tvMyDate.setText(R.string.change_date);
                }
                unitBinding.tvMyDate.setVisibility(View.VISIBLE);


                String role = mDataManager.getLoginPrefs().getRole();


                if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name()) &&
                        !TextUtils.isEmpty(model.getStatus())) {
                    try {
                        switch (UnitStatusType.valueOf(model.getStatus())) {
                            case Completed:
                                unitBinding.card.setBackgroundColor(
                                        ContextCompat.getColor(getContext(), R.color.secondary_green));
                                break;
                            case InProgress:
                                unitBinding.card.setBackgroundColor(
                                        ContextCompat.getColor(getContext(), R.color.humana_card_background));
                                break;
                            case Pending:
                                unitBinding.card.setBackgroundColor(ContextCompat.getColor(getContext(),
                                        R.color.material_red_500));
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        unitBinding.statusIcon.setVisibility(View.GONE);
                    }
                } else {
                    unitBinding.statusIcon.setVisibility(View.GONE);
                }
                if (model.getType().toLowerCase().equals(mActivity.getString(R.string.course).toLowerCase())) {
                    if (mDataManager.getLoginPrefs().getRole() != null
                            && mDataManager.getLoginPrefs().getRole()
                            .equals(UserRole.Student.name())) {
                        unitBinding.tvMyDate.setEnabled(false);
                    } else {
                        unitBinding.tvMyDate.setEnabled(true);
                    }
                } else {
                    unitBinding.tvMyDate.setEnabled(true);
                }
                if (role != null && role.equals(UserRole.Student.name())) {
                    unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.student_icon,
                            0, 0, 0);
                } else {
                    unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.teacher_icon,
                            0, 0, 0);
                }
                switch (model.getStatus()) {
                    case "Submitted":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.pending));
                        break;
                    case "Approved":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_green));
                        break;
                    case "Return":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_red));
                        break;
                    case "":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        break;
                    case "None":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        break;
                }
                unitBinding.tvMyDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                unitBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }

    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            searchText.set(s.toString());

        }
    };

    public void searchUnits() {
        changesMade = true;
        fetchData();
    }


    public void changeToCalenderView(Boolean isChecked) {
        if (isChecked) {
            isCheckedObserver.set(true);
            calVisible.set(true);
            frameVisible.set(false);
            changesMade = false;
            skip = 0;
            fetchData();
        } else {
            isCheckedObserver.set(isChecked);
            calVisible.set(false);
            frameVisible.set(true);
            fetchData();
        }
    }

}
