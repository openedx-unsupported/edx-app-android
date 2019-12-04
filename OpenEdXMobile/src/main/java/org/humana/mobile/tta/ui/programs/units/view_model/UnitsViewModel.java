package org.humana.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowFilterDropDownBinding;
import org.humana.mobile.databinding.TRowTextBinding;
import org.humana.mobile.databinding.TRowUnitBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.ShowIn;
import org.humana.mobile.tta.data.enums.UnitStatusType;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.ContentList;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.tta.ui.mxCalenderView.Events;
import org.humana.mobile.tta.ui.programs.units.UnitCalendarActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class UnitsViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;
    public ProgramUser user;
    private int filterSize = 0;

    public static ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public static ObservableBoolean calVisible = new ObservableBoolean();
    public static ObservableBoolean frameVisible = new ObservableBoolean();
    public static List<Events> eventsArrayList = new ArrayList<>();
    public static ObservableField switchText = new ObservableField<>();
    public static ObservableField selectedEvent = new ObservableField<>();
    public List<ProgramFilterTag> selectedTags = new ArrayList<>();

    public ObservableField<String> searchText = new ObservableField<>("");

    private EnrolledCoursesResponse course;
    private List<Unit> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private EnrolledCoursesResponse parentCourse;

    public ObservableBoolean filterSelected = new ObservableBoolean();
    public ObservableBoolean contentListSelected = new ObservableBoolean();
    public ObservableInt selectedContentListPosition = new ObservableInt(0);
    public ObservableField<String> contentListText = new ObservableField<>();
    public String selectedSession;


    private boolean isAllLoaded = false;
    private ContentList selectedContentList;
    private boolean isSelected = false;
    private List<DropDownFilterView.FilterItem> sessionTags;


    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };


    public UnitsViewModel(Context context, TaBaseFragment fragment, EnrolledCoursesResponse course) {
        super(context, fragment);

        this.course = course;
        units = new ArrayList<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;
        calVisible.set(false);
        frameVisible.set(true);


        unitsAdapter = new UnitsAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
        switchText.set("Calendar View");
        selectedSession = "";
        unitsAdapter.setItems(units);
        unitsAdapter.setItemClickListener((view, item) -> {

            switch (view.getId()) {
                case R.id.tv_my_date:
                    showDatePicker(item);
                    break;
                default:
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
                                                        UnitsViewModel.this.course = response;
                                                        EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                    } else {
                                                        UnitsViewModel.this.parentCourse = response;
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
                   /* mDataManager.enrolInCourse(mDataManager.getLoginPrefs().getProgramId(),
                            new OnResponseCallback<ResponseBody>() {
                                @Override
                                public void onSuccess(ResponseBody responseBody) {
                                    mDataManager.getBlockComponent(item.getId(), mDataManager.getLoginPrefs().getProgramId(),
                                            new OnResponseCallback<CourseComponent>() {
                                                @Override
                                                public void onSuccess(CourseComponent data) {
                                                    mActivity.hideLoading();

                                                    if (UnitsViewModel.this.course == null) {
                                                        mActivity.showLongSnack("You're not enrolled in the program");
                                                        return;
                                                    }

                                                    if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                                        mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                                mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                                UnitsViewModel.this.course, data.getChildren().get(0).getId(),
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
                                public void onFailure(Exception e) {
                                    mActivity.showLongSnack("error during unit enroll");
                                }
                            });
*/
            }

        });

        mActivity.showLoading();
        fetchFilters();
        fetchData();
    }

    private void getBlockComponent(Unit unit) {

        mDataManager.enrolInCourse(mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        mDataManager.getBlockComponent(unit.getId(), mDataManager.getLoginPrefs().getProgramId(),
                                new OnResponseCallback<CourseComponent>() {
                                    @Override
                                    public void onSuccess(CourseComponent data) {
                                        mActivity.hideLoading();

                                        if (UnitsViewModel.this.course == null) {
                                            mActivity.showLongSnack("You're not enrolled in the program");
                                            return;
                                        }

                                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                    UnitsViewModel.this.course, data.getChildren().get(0).getId(),
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
                    public void onFailure(Exception e) {
                        mActivity.showLongSnack("error during unit enroll");
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
        calVisible.set(false);
//        onEventMainThread(units);

    }


    private void showDatePicker(Unit unit) {
        DateUtil.showDatePicker(mActivity, unit.getMyDate(), new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                mActivity.showLoading();
                mDataManager.setProposedDate(mDataManager.getLoginPrefs().getProgramId(),
                        mDataManager.getLoginPrefs().getSectionId(), data,
                        unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
                                unit.setMyDate(data);
                                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));
                                if (response.getSuccess()) {
                                    mActivity.showLongSnack("Proposed date set successfully");
                                    EventBus.getDefault().post(units);
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
            public void onFailure(Exception e) {

            }
        });
    }

    public void fetchFilters() {
        sessionTags = new ArrayList<>();

        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.units.name(), filters,
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {
                        if (!data.isEmpty()) {
                            allFilters = data;
                            filterSize = allFilters.size();
                            filtersVisible.set(true);
                            filtersAdapter.setItems(allFilters);


                         /*   for (ProgramFilter filter : data) {
                                sessionTags.clear();
                                isSelected = filter.getSelected();
                                if (filter.getInternalName().toLowerCase().contains("session_id")) {
                                    sessionTags.clear();
                                    sessionTags.add(new DropDownFilterView.FilterItem(filter.getDisplayName(), null,
                                            isSelected, R.color.primary_cyan, R.drawable.t_background_tag_hollow));

                                    for (ProgramFilterTag tag : filter.getTags()) {
                                        if (mDataManager.getLoginPrefs().getSessionFilter()!=null) {
                                            if (mDataManager.getLoginPrefs().getSessionFilter().equals(tag.getDisplayName())) {
                                                isSelected = true;
                                                sessionTags.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                                                        isSelected, R.color.white, R.drawable.t_background_tag_filled));

                                            } else {
                                                sessionTags.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                                                        false, R.color.white, R.drawable.t_background_tag_filled));
                                            }
                                        }else {
                                            sessionTags.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                                                    isSelected, R.color.white, R.drawable.t_background_tag_filled));
                                        }

                                        try {
                                            if (tag.getSelected()) {
                                                tags.clear();
                                                tags.add(tag);
                                                changesMade = true;
                                                allLoaded = false;
                                                fetchData();
                                            }
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }*/

                        } else {
                            filtersVisible.set(false);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        filtersVisible.set(false);
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

        fetchUnits();

    }

    private void setUnitFilters() {
        filters.clear();
        if (tags.isEmpty() || allFilters == null || allFilters.isEmpty()) {
            return;
        }

        for (ProgramFilter filter : allFilters) {

            List<ProgramFilterTag> selectedTags = new ArrayList<>();
            for (ProgramFilterTag tag : filter.getTags()) {
                if (tags.contains(tag)) {
                    selectedTags.add(tag);
                }
            }

            if (!selectedTags.isEmpty()) {
                ProgramFilter pf = new ProgramFilter();
                pf.setDisplayName(filter.getDisplayName());
                pf.setInternalName(filter.getInternalName());
                pf.setId(filter.getId());
                pf.setOrder(filter.getOrder());
                pf.setShowIn(filter.getShowIn());
                pf.setTags(selectedTags);

                filters.add(pf);
            }
        }
    }

    public void fetchUnits() {
//        if (mDataManager.getLoginPrefs().getstoreSelectedTags() != null) {
//            filters.clear();
//            tags.clear();
//            for (int i = 0; i < mDataManager.getLoginPrefs().getstoreSelectedTags().getTags().size(); i++) {
//                ProgramFilterTag pt = new ProgramFilterTag();
//                pt.setSelected(mDataManager.getLoginPrefs().getstoreSelectedTags().getTags().get(i).getSelected());
//                pt.setDisplayName(mDataManager.getLoginPrefs().getstoreSelectedTags().getTags().get(i).getDisplayName());
//                pt.setId(mDataManager.getLoginPrefs().getstoreSelectedTags().getTags().get(i).getId());
//                pt.setInternalName(mDataManager.getLoginPrefs().getstoreSelectedTags().getTags().get(i).getInternalName());
//                pt.setOrder(mDataManager.getLoginPrefs().getstoreSelectedTags().getTags().get(i).getOrder());
//                tags.add(pt);
//            }
//            ProgramFilter pf = new ProgramFilter();
//            pf.setDisplayName(mDataManager.getLoginPrefs().getstoreSelectedTags().getDisplayName());
//            pf.setInternalName(mDataManager.getLoginPrefs().getstoreSelectedTags().getInternalName());
//            pf.setId(mDataManager.getLoginPrefs().getstoreSelectedTags().getId());
//            pf.setOrder(mDataManager.getLoginPrefs().getstoreSelectedTags().getOrder());
//            pf.setShowIn(mDataManager.getLoginPrefs().getstoreSelectedTags().getShowIn());
//            pf.setTags(tags);
//            filters.add(pf);
//        }
        mDataManager.getUnits(filters, searchText.get(), mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole(),
                "", 0L, take, skip, 0L, 0L,
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
                        toggleEmptyVisibility();
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
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PeriodSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        fetchData();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(List<Unit> unit) {
//        filters.clear();
        changesMade = true;
        allLoaded = false;
//        fetchFilters();
        fetchData();


    }

    @SuppressWarnings("unused")
    public void onEventMainThread(String sessiontype) {
        if (!org.humana.mobile.tta.data.constants.Constants.selectedSession.equals("")) {
            org.humana.mobile.tta.data.constants.Constants.selectedSession = sessiontype;
            fetchFilters();
            fetchData();
            changesMade = true;
            allLoaded = false;

        }
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
        public int getItemLayout(int position) {

            if (user == null) {
                return R.layout.t_row_filter_drop_down;
            } else {
                return R.layout.t_row_text;
            }
        }

        public int getItemCount() {
            if (user != null) {
                return filterSize + 1;
            } else {
                return filterSize;
            }
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model, @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof TRowFilterDropDownBinding) {
                TRowFilterDropDownBinding dropDownBinding = (TRowFilterDropDownBinding) binding;

                int langPos=0;
                int sessionPos=0;

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

                for (int i=0; i<items.size();i++){
                    if (mDataManager.getLoginPrefs().getSessionFilter()!=null) {
                        if (mDataManager.getLoginPrefs().getSessionFilter().equals(items.get(i).getName())) {
                            sessionPos = i;
                        }
                    }
                }
                for (int i=0; i<items.size();i++){
                    if (mDataManager.getLoginPrefs().getLangTag()!=null) {
                        if (mDataManager.getLoginPrefs().getLangTag().equals(items.get(i).getName())) {
                            langPos = i;
                        }
                    }
                }
                dropDownBinding.filterDropDown.setFilterItems(items);


                if (model.getInternalName().toLowerCase().contains("session_id")) {
                    dropDownBinding.filterDropDown.setSelection(sessionPos);
                    dropDownBinding.filterDropDown.notifyDataSetChanged();
                }

                if (model.getInternalName().toLowerCase().contains("language_id")) {
                    dropDownBinding.filterDropDown.setSelection(langPos);
                    dropDownBinding.filterDropDown.notifyDataSetChanged();
                }

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
                    if (prev != null && prev.getItem() != null) {
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null) {
                        tags.add((ProgramFilterTag) item.getItem());
                        selectedTags.add((ProgramFilterTag) item.getItem());
                    }

                    if (!Objects.requireNonNull(mDataManager.getLoginPrefs().getProgramFilters()).contains(model)) {
                        mDataManager.getLoginPrefs().storeProgramFilter(model);
                    }
                    if (!Objects.equals(mDataManager.getLoginPrefs().getStoreSessionFilterTag(), item.getItem())) {
                        mDataManager.getLoginPrefs().storeSessionFilterTag((ProgramFilterTag) item.getItem());
                    }
                    if (model.getInternalName().toLowerCase().contains("session_id")){
                        mDataManager.getLoginPrefs().setSessionFilter(item.getName());
                    }

                    if (model.getInternalName().toLowerCase().contains("language_id")){
                        mDataManager.getLoginPrefs().setLangTag(item.getName());
                    }

                    if (mDataManager.getLoginPrefs().getTags()!=null) {
                        mDataManager.getLoginPrefs().clearTags();
                        mDataManager.getLoginPrefs().storeTags(tags);
                    }else{
                        mDataManager.getLoginPrefs().clearTags();
                        mDataManager.getLoginPrefs().storeTags(tags);
                    }
                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    fetchFilters();
                    fetchData();
                });

        } else if (binding instanceof TRowTextBinding) {
                TRowTextBinding textBinding = (TRowTextBinding) binding;
                textBinding.text.setText(user.name);
            }
        }
    }

    public class UnitsAdapter extends MxInfiniteAdapter<Unit> {
        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model, @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowUnitBinding) {
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);

                unitBinding.unitCode.setText(model.getTitle());
                unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                        + model.getUnitHour() + " " + mActivity.getResources().getString(R.string.point_txt));
                if (!model.getStatus().equals("")) {
                    if (model.getStatusDate() > 0) {
                        unitBinding.tvStaffDate.setText(model.getStatus() + ": " + DateUtil.getDisplayDate(model.getStatusDate()));
                        unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                    }
                } else {
                    unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                }
                unitBinding.tvDescription.setText(model.getDesc());
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
//                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())){
//                    if (model.getStatus().equals("Submitted")){
//                        unitBinding.tvComment.setVisibility(View.GONE);
//                    }else {
//                        unitBinding.tvComment.setVisibility(View.VISIBLE);
//                    }
//                }
                unitBinding.checkbox.setVisibility(View.GONE);

                if (model.getMyDate() > 0) {
                    unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                } else {
                    unitBinding.tvMyDate.setText(R.string.proposed_date);
                }

                String role = mDataManager.getLoginPrefs().getRole();
                if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                    if (model.getStaffDate() > 0) {
                        unitBinding.tvSubmittedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                        unitBinding.tvSubmittedDate.setVisibility(View.VISIBLE);
                    } else {
                        unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                    }
                } else {
                    unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);

                }

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

    public void changeToCalenderView() {
        ActivityUtil.gotoPage(mActivity, UnitCalendarActivity.class);
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

    public void setSessionFilter() {
        if (mDataManager.getLoginPrefs().getProgramFilters() != null) {
//            changesMade = true;
//            allLoaded = false;
            mActivity.showLoading();
            tags.clear();
            tags = mDataManager.getLoginPrefs().getTags() ;
            fetchFilters();
            fetchUnits();
        }


    }

}
