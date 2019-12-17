package org.humana.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowUnitBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.UnitStatusType;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseBottomsheetFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.mxCalenderView.CustomCalendarView;
import org.humana.mobile.tta.ui.mxCalenderView.Events;
import org.humana.mobile.tta.ui.programs.units.PeriodListingActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class CalendarBottomSheetViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 0;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
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
    public ObservableField<String> dispDate = new ObservableField<>();
    public BottomSheetBehavior sheetBehavior;


    private EnrolledCoursesResponse course;
    private List<Unit> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private EnrolledCoursesResponse parentCourse;
    private String selectedDate;
    private long startDateTime, endDateTime, eventMonthYear;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
//        if (allLoaded)
            return false;
//        this.skip++;
////        fetchData();
//        return true;
    };
    public CalendarBottomSheetViewModel(Context context, TaBaseBottomsheetFragment fragment, EnrolledCoursesResponse course,
                                        Long selectedDate, Long startDateTime, Long endDateTime) {
        super(context, fragment);


        this.course = course;
        this.units = units;
        this.selectedDate = DateUtil.getDisplayDate(selectedDate);
        this.eventMonthYear =selectedDate;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        dispDate.set(DateUtil.getCalendarDate(selectedDate));
//        units = new ArrayList<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;
        calVisible.set(false);
        frameVisible.set(true);

        unitsAdapter = new UnitsAdapter(mActivity);
//        switchText.set("Calendar View");

//        unitsAdapter.setItems(units);
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
                                                        CalendarBottomSheetViewModel.this.course = response;
                                                        EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                    } else {
                                                        CalendarBottomSheetViewModel.this.parentCourse = response;
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

            }

        });

        mActivity.showLoading();
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

                                        if (CalendarBottomSheetViewModel.this.course == null) {
                                            mActivity.showLongSnack("You're not enrolled in the program");
                                            return;
                                        }

                                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                    CalendarBottomSheetViewModel.this.course, data.getChildren().get(0).getId(),
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
//        onEventMainThread(units);

    }


    private void showDatePicker(Unit unit) {
        DateUtil.showDatePicker(mActivity, unit.getMyDate(), new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                mActivity.showLoading();
                mDataManager.setProposedDate(mDataManager.getLoginPrefs().getProgramId(),
                        mDataManager.getLoginPrefs().getSectionId(), data, unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
//                                Events event = new Events(DateUtil.getDisplayDate(unit.getMyDate()), unit.getTitle(), unit.getType());
//                                eventsArrayList.remove(event);

                                unit.setMyDate(data);
                                unitsAdapter.remove(unit);
                                unitsAdapter.notifyDataSetChanged();
//                                eventMonthYear = data;
//                                selectedDate = DateUtil.getDisplayDate(data);


//                                Events event1 = new Events(DateUtil.getDisplayDate(data), unit.getTitle(), unit.getType());
//                                eventsArrayList.add(event1);

//                                units.remove(unit);
                                if (units.size()==0){
                                    emptyVisible.set(true);
                                }else {
                                    emptyVisible.set(false);
                                }
//                                selectedDate = DateUtil.getDisplayDate(data);
//                                if (response.getSuccess()) {
//                                    eventsArrayList.clear();
//                                    for (int i = 0; i < units.size(); i++) {
//                                        if (units.get(i).getMyDate() > 0) {
//                                            Events et = new Events(DateUtil.getDisplayDate(units.get(i).getMyDate()),
//                                                    units.get(i).getTitle());
//                                            eventsArrayList.add(et);
//                                        }
//                                    }
                                    mActivity.showLongSnack("Proposed date set successfully");
                                mActivity.hideLoading();
//                                EventBus.getDefault().post(units);
//                                CustomCalendarView.createEvents(eventsArrayList, eventMonthYear);
                                fetchUnits();
//                                }
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



    private void fetchData() {

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

    private void fetchUnits() {

        mDataManager.getUnits(filters, "",mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole(), "",
                0L, take, skip,
                startDateTime, endDateTime,
                new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        units = new ArrayList<>();
                        eventsArrayList.clear();
                        String role = mDataManager.getLoginPrefs().getRole();
                        if (role.equals(UserRole.Student.name())) {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getCommonDate() > 0) {
                                    if (DateUtil.getDisplayDate(data.get(i).getCommonDate()).equals(selectedDate)) {
                                        units.add(data.get(i));
//                                        Unit unit = data.get(i);
//                                        units.add(unit);
//                                populateUnits(units);
                                    }
                                }
                            }
                        }else {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getMyDate() > 0) {
                                    if (DateUtil.getDisplayDate(data.get(i).getMyDate()).equals(selectedDate)) {
                                        units.add(data.get(i));
//                                        units =data;
//                                populateUnits(units);
                                    }
                                }
                            }
                        }

                        unitsAdapter.setItems(units);
                        unitsAdapter.notifyDataSetChanged();
                        unitsAdapter.setLoadingDone();

                        if (role.equals(UserRole.Instructor.name())) {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getMyDate() > 0) {
                                    Events et = new Events(DateUtil.getDisplayDate(data.get(i).getMyDate()),
                                            data.get(i).getTitle(), data.get(i).getType());
                                    eventsArrayList.add(et);
                                }
                            }
                        }else {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getCommonDate() > 0) {
                                    Events et = new Events(DateUtil.getDisplayDate(data.get(i).getCommonDate()),
                                            data.get(i).getTitle(), data.get(i).getType());
                                    eventsArrayList.add(et);
                                }
                            }
                        }
//
                        CustomCalendarView.createEvents(eventsArrayList, eventMonthYear);

//                        populateUnits(data);
                        if (units.size()==0){
                            emptyVisible.set(true);
                        }
//                        unitsAdapter.setItems(units);
//                        unitsAdapter.notifyDataSetChanged();
//                        unitsAdapter.setLoadingDone();
                        mActivity.hideLoading();
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
//        fetchData();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(List<Unit> unit) {
//        filters.clear();
        changesMade = true;
        allLoaded = false;
//        fetchData();
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


    public class UnitsAdapter extends MxInfiniteAdapter<Unit> {
        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model, @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowUnitBinding) {


//                unitBinding.tvStaffDate.setVisibility(View.GONE);
//                unitBinding.tvMyDate.setVisibility(View.GONE);
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                    if (DateUtil.getDisplayDate(model.getCommonDate()).equals(selectedDate)) {
//                    CustomCalendarView.createEvents(eventsArrayList);
                        TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                        unitBinding.setUnit(model);
                        emptyVisible.set(false);
                        unitBinding.unitCode.setText(model.getTitle());
                        unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                                + model.getUnitHour() +" "+ mActivity.getResources().getString(R.string.point_txt));
                        if (!model.getStatus().isEmpty()) {
                            if (model.getStaffDate() > 0) {
                                unitBinding.tvStaffDate.setText(model.getStatus() + " : " + DateUtil.getDisplayDate(model.getStatusDate()));
                                unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                            }
                        } else {
                            unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                        }
                        unitBinding.tvDescription.setText(model.getDesc());
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                            if (model.getComment() != null) {
                                unitBinding.tvComment.setText(model.getComment());
                            } else {
                                unitBinding.tvComment.setVisibility(View.GONE);
                            }
                        } else {
                            unitBinding.tvComment.setVisibility(View.GONE);
                        }
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

//                    Events ev = new Events(DateUtil.getDisplayDate(model.getStaffDate()));
//                    eventsArrayList.add(ev);
//                    CustomCalendarView.createEvents(eventsArrayList);

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
                    } else {
                        emptyVisible.set(true);
                    }
                }
                else {
                    if (DateUtil.getDisplayDate(model.getMyDate()).equals(selectedDate)) {
//                    CustomCalendarView.createEvents(eventsArrayList);
                        TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                        unitBinding.setUnit(model);
                        emptyVisible.set(false);
                        unitBinding.unitCode.setText(model.getTitle());
                        unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                                + model.getUnitHour() + " "+mActivity.getResources().getString(R.string.point_txt));
                        if (!model.getStatus().isEmpty()) {
                            if (model.getStaffDate() > 0) {
                                unitBinding.tvStaffDate.setText(model.getStatus() + " : " + DateUtil.getDisplayDate(model.getStatusDate()));
                                unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                            }
                        } else {
                            unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                        }
                        unitBinding.tvDescription.setText(model.getDesc());
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                            if (model.getComment() != null) {
                                unitBinding.tvComment.setText(model.getComment());
                            } else {
                                unitBinding.tvComment.setVisibility(View.GONE);
                            }
                        } else {
                            unitBinding.tvComment.setVisibility(View.GONE);
                        }
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
                    } else {
                        emptyVisible.set(true);
                    }
                }
            }
        }
    }

    public void navigateToPeriodListing(){
        ActivityUtil.gotoPage(mActivity, PeriodListingActivity.class);
    }
}
