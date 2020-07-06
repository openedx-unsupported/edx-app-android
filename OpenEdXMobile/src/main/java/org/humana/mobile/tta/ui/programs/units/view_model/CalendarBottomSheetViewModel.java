package org.humana.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

import com.lib.mxcalendar.models.Event;
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
import org.humana.mobile.tta.data.model.program.UnitPublish;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.event.program.ProgramFilterSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseBottomsheetFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.programs.addunits.AddUnitsActivity;
import org.humana.mobile.tta.ui.programs.addunits.viewmodel.AddUnitsViewModel;
import org.humana.mobile.tta.ui.programs.units.PeriodListingActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.tta.utils.AppUtil;
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
    public static List<Event> eventsArrayList = new ArrayList<>();
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
    private long startDateTime, endDateTime, selectedDateLng;
    private final String SELECTED_DATE = "selected_date";
    private Boolean isDateSelected;
    public ObservableBoolean progressVisible = new ObservableBoolean();
    private Long periodId;
    private String periodName;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
//        if (allLoaded)
        return false;
//        this.skip++;
////        fetchData();
//        return true;
    };

    public CalendarBottomSheetViewModel(Context context, TaBaseBottomsheetFragment fragment, EnrolledCoursesResponse course,
                                        Long selectedDate, Long startDateTime, Long endDateTime,
                                        Long periodId, String periodName, List<ProgramFilter> filters) {
        super(context, fragment);


        this.course = course;
        this.units = units;
        this.selectedDate = DateUtil.getDisplayDate(selectedDate);
        this.selectedDateLng = selectedDate;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.periodId = periodId;
        this.periodName = periodName;
        this.filters = filters;

        dispDate.set(DateUtil.getCalendarDate(selectedDate));
//        units = new ArrayList<>();
        tags = new ArrayList<>();
//        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;
        calVisible.set(false);
        frameVisible.set(true);
        isDateSelected = true;

        unitsAdapter = new UnitsAdapter(mActivity);
//        switchText.set("Calendar View");

//        unitsAdapter.setItems(units);
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
                    getUnitPublish(item);

            }

        });

        mActivity.showLoading();
//        progressVisible.set(true);
        fetchData();
    }

    private void getUnitPublish(Unit unit) {
        String unitId = AppUtil.encode(unit.getId());
        mDataManager.getUnitPublish(unitId,
                new OnResponseCallback<UnitPublish>() {
                    @Override
                    public void onSuccess(UnitPublish data) {
                        if (data !=null && data.isPublish){
                            mActivity.showLoading();

                            boolean ssp = units.contains(unit);
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
                                                            getBlockComponent(unit);
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
                                getBlockComponent(unit);
                            }
                        }
                        else{
                            mActivity.showShortSnack(mActivity.getString(R.string.unit_not_published));
                            mActivity.hideLoading();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.showShortSnack(mActivity.getString(R.string.unit_not_published));
                        mActivity.hideLoading();
                    }
                });

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

                                mActivity.showLongSnack("Proposed date set successfully");
//                                mActivity.hideLoading();
                                isDateSelected = true;
                                fetchUnits();
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
//        mActivity.showLoading();
        fetchUnits();

    }



    private void fetchUnits() {

        mDataManager.getUnits(filters, "", mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole(), "",
                0L, take, skip, startDateTime, endDateTime,
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

                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getMyDate() > 0) {
                                    if (DateUtil.getDisplayDate(data.get(i).getMyDate()).equals(selectedDate)) {
                                        units.add(data.get(i));

                                    }
                                }
                            }
                        }

                        unitsAdapter.setItems(units);
                        unitsAdapter.notifyDataSetChanged();
                        unitsAdapter.setLoadingDone();
                        progressVisible.set(false);
                        if (unitsAdapter.isEmpty()) {
                            emptyVisible.set(true);
                        }
                        String colorCode = "#ffffff";
                        Event et;

                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                            for (int i = 0; i < units.size(); i++) {
                                if (units.get(i).getCommonDate() > 0) {
                                    switch (units.get(i).getType()) {
                                        case "Study Task":
                                            colorCode = "#F8E56B";
                                            et = new Event(DateUtil.getDisplayDate(units.get(i).getCommonDate()),
                                                    units.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Experience":
                                            colorCode = "#33FFAC";
                                            et = new Event(DateUtil.getDisplayDate(units.get(i).getCommonDate()),
                                                    units.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Course":
                                            colorCode = "#EF98FC";
                                            et = new Event(DateUtil.getDisplayDate(units.get(i).getCommonDate()),
                                                    units.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                    }

                                }
                            }
                        } else {

                            for (int i = 0; i < units.size(); i++) {
                                if (units.get(i).getMyDate() > 0) {
                                    switch (units.get(i).getType()) {
                                        case "Study Task":
                                            colorCode = "#F8E56B";
                                            et = new Event(DateUtil.getDisplayDate(units.get(i).getMyDate()),
                                                    units.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Experience":
                                            colorCode = "#33FFAC";
                                            et = new Event(DateUtil.getDisplayDate(units.get(i).getMyDate()),
                                                    units.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                        case "Course":
                                            colorCode = "#EF98FC";
                                            et = new Event(DateUtil.getDisplayDate(units.get(i).getMyDate()),
                                                    units.get(i).getTitle(), null, colorCode);
                                            eventsArrayList.add(et);
                                            break;
                                    }

                                }
                            }
                        }

                        if (isDateSelected) {
                            EventBus.getDefault().post(eventsArrayList);
                            isDateSelected = false;
                        }
                        progressVisible.set(false);
                        mActivity.hideLoading();
                        unitsAdapter.setLoadingDone();

                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        progressVisible.set(false);
                        unitsAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
        mActivity.hideLoading();

    }

    private void toggleEmptyVisibility() {
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void onEventMainThread(PeriodSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        fetchData();
    }

    public void onEventMainThread(ProgramFilterSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        filters = new ArrayList<>();
        filters = event.getProgramFilters();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(List<Unit> unit) {
        changesMade = true;
        allLoaded = false;
        fetchData();
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

                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                    if (DateUtil.getDisplayDate(model.getCommonDate()).equals(selectedDate)) {
                        TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                        unitBinding.setUnit(model);
                        progressVisible.set(false);
                        unitBinding.unitCode.setText(model.getTitle());
                        unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                                + model.getUnitHour() + " " + mActivity.getResources().getString(R.string.point_txt));
                        if (!model.getStatus().isEmpty()) {
                            if (model.getStaffDate() > 0) {
                                if (!model.getStatus().matches("None")) {
                                    unitBinding.tvStaffDate.setText(model.getStatus() + " : " + DateUtil.getDisplayDate(model.getStatusDate()));
                                    unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                                }else{
                                    unitBinding.tvStaffDate.setText(DateUtil.getDisplayDate(model.getStatusDate()));
                                    unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                                }
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
                            unitBinding.tvMyDate.setText(R.string.change_date);
                        }

                        String role = mDataManager.getLoginPrefs().getRole();
                        if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                            if (model.getStaffDate() > 0) {
                                if (model.getStatus().toLowerCase() != "none") {
                                unitBinding.tvSubmittedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                                unitBinding.tvSubmittedDate.setVisibility(View.VISIBLE);
                                }
                            } else {
                                unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
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
                    } else {
                        emptyVisible.set(true);
                        progressVisible.set(false);
                    }
                } else {
                    if (DateUtil.getDisplayDate(model.getMyDate()).equals(selectedDate)) {
                        TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                        unitBinding.setUnit(model);
                        emptyVisible.set(false);
                        progressVisible.set(false);
                        unitBinding.unitCode.setText(model.getTitle());
                        unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                                + model.getUnitHour() + " " + mActivity.getResources().getString(R.string.point_txt));
                        if (!model.getStatus().isEmpty()) {
                            if (model.getStaffDate() > 0) {
                                if (model.getStatus().toLowerCase() != "none") {
                                    unitBinding.tvStaffDate.setText(model.getStatus() + " : " + DateUtil.getDisplayDate(model.getStatusDate()));
                                    unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                                }else{
                                    unitBinding.tvStaffDate.setText(DateUtil.getDisplayDate(model.getStatusDate()));
                                    unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                        }
                        unitBinding.tvDescription.setText(model.getDesc());
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {

                            if (model.getComment() != null || model.getComment() != "") {
                                if (model.getComment().length() > 20) {
                                    unitBinding.tvComment.setText(model.getStatus() + " comments : " + model.getComment());
                                    unitBinding.tvRead.setMovementMethod(new ScrollingMovementMethod());
                                    unitBinding.tvRead.setVisibility(View.VISIBLE);
                                } else {
                                    unitBinding.tvComment.setVisibility(View.VISIBLE);
                                    unitBinding.tvComment.setText(model.getComment());
                                    unitBinding.tvRead.setVisibility(View.GONE);
                                }
                            } else {

                                unitBinding.tvComment.setVisibility(View.GONE);
                                unitBinding.tvRead.setVisibility(View.GONE);
                            }
                        } else {
                            unitBinding.tvComment.setVisibility(View.GONE);
                            unitBinding.tvRead.setVisibility(View.GONE);
                        }

                        if (model.getMyDate() > 0) {
                            unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                        } else {
                            unitBinding.tvMyDate.setText(R.string.change_date);
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


                        if (role != null && role.equals(UserRole.Student.name())) {
                            unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.student_icon,
                                    0, 0, 0);
                        } else {
                            unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.teacher_icon,
                                    0, 0, 0);
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

                    /*    if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name()) &&
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
                        }*/


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
                        progressVisible.set(false);
                    }
                }
                progressVisible.set(false);

            }
        }
    }

    public void navigateToPeriodListing() {
        if (periodId > 0) {
            Bundle parameters = new Bundle();
            parameters.putLong(Constants.KEY_PERIOD_ID, periodId);
            parameters.putString(Constants.KEY_PERIOD_NAME, periodName);
            parameters.putLong(Constants.SELECTED_DATE, selectedDateLng);
            ActivityUtil.gotoPage(mActivity, AddUnitsActivity.class, parameters);
        } else {
            Intent intent = new Intent(mActivity, PeriodListingActivity.class);
            intent.putExtra(Constants.SELECTED_DATE, selectedDateLng);
            intent.putExtra(Constants.KEY_PERIOD_ID, periodId);
            mActivity.startActivity(intent);
        }
    }


}
