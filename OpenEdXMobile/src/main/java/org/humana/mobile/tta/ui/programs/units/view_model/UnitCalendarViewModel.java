package org.humana.mobile.tta.ui.programs.units.view_model;

import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowUnitBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.UnitStatusType;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.CalendarEvents;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.mxCalenderView.CustomCalendarView;
import org.humana.mobile.tta.ui.mxCalenderView.Events;
import org.humana.mobile.tta.ui.programs.units.ActivityCalendarBottomSheet;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class UnitCalendarViewModel extends BaseViewModel {
    private static final int DEFAULT_TAKE = 0;
    private static final int DEFAULT_SKIP = 0;
    static BottomSheetBehavior sheetBehavior;

    public static UnitsAdapter unitsAdapter;
    public RecyclerView.LayoutManager layoutManager;
    public ProgramUser user;
    private int filterSize = 0;

    public static ObservableBoolean filtersVisible = new ObservableBoolean();
    public static ObservableBoolean emptyVisible = new ObservableBoolean();
    public static ObservableBoolean calVisible = new ObservableBoolean();
    public static ObservableBoolean frameVisible = new ObservableBoolean();
    public static ObservableBoolean setSelected = new ObservableBoolean();
    public static List<Events> eventsArrayList = new ArrayList<>();
    public static ObservableField switchText = new ObservableField<>();
    public static ObservableField selectedEvent = new ObservableField<>();
    public static ObservableField<String> weekNo = new ObservableField<>();


    private EnrolledCoursesResponse course;
    private static List<Unit> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private EnrolledCoursesResponse parentCourse;
    public static ObservableField<String> eventDate = new ObservableField<>();
    public static ObservableField<String> dispDate = new ObservableField<>();

    public static long eventDisplayDate = 0L;
    public static long startDateTime, endDateTime;


    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public UnitCalendarViewModel(BaseVMActivity activity, EnrolledCoursesResponse course) {
        super(activity);

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

        layoutManager = new LinearLayoutManager(mActivity);
        unitsAdapter = new UnitsAdapter(mActivity);
        setSelected.set(false);


        eventsArrayList.clear();
        unitsAdapter.setItems(units);
//        unitsAdapter.notifyDataSetChanged();
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
                                                        UnitCalendarViewModel.this.course = response;
                                                        EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                    } else {
                                                        UnitCalendarViewModel.this.parentCourse = response;
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
//        CustomCalendarView.setupAdapter();


    }

    private void fetchData() {

        if (changesMade) {
            changesMade = false;
            skip = 0;
            unitsAdapter.reset(true);
        }

        fetchUnits();

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

                                        if (UnitCalendarViewModel.this.course == null) {
                                            mActivity.showLongSnack("You're not enrolled in the program");
                                            return;
                                        }

                                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                    UnitCalendarViewModel.this.course, data.getChildren().get(0).getId(),
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
                                unit.setMyDate(data);
                                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));

                                if (response.getSuccess()) {
                                    mActivity.showLongSnack("Proposed date set successfully");
//                                    eventsArrayList.clear();
                                    fetchData();
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

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
//        fetchUnits();

    }

    public void fetchUnits() {

        mDataManager.getUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),  mDataManager.getLoginPrefs().getRole(), "",
                0L, take, skip, startDateTime,endDateTime,
                new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        units = data;
                        changesMade = false;
                        populateUnits(data);
                        eventsArrayList.clear();
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getCommonDate() > 0) {
                                    Events et = new Events(DateUtil.getDisplayDate(data.get(i).getCommonDate()),
                                            data.get(i).getTitle(), data.get(i).getType());
                                    eventsArrayList.add(et);
                                }
                            }
                        }else {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getMyDate() > 0) {
                                    Events et = new Events(DateUtil.getDisplayDate(data.get(i).getMyDate()),
                                            data.get(i).getTitle(), data.get(i).getType());
                                    eventsArrayList.add(et);
                                }
                            }
                        }
                        CustomCalendarView.createEvents(eventsArrayList, eventDisplayDate);
                        mActivity.hideLoading();

//                        unitsAdapter.setItems(data);
//                        unitsAdapter.notifyDataSetChanged();
//                        unitsAdapter.setLoadingDone();
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

    private void fetchEvents() {

        mDataManager.getEventCalendar(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),  mDataManager.getLoginPrefs().getRole(), take,skip, 3,eventDisplayDate,
                new OnResponseCallback<List<CalendarEvents>>() {
                    @Override
                    public void onSuccess(List<CalendarEvents> data) {
                        mActivity.hideLoading();

                        for (int i = 0 ; i<data.size(); i++){
                            CustomCalendarView.createEvents(eventsArrayList, eventDisplayDate);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
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
    public void onEventMainThread(List<Unit> units) {
//        filters.clear();
//        changesMade = true;
//        allLoaded = false;
//        fetchData();

//        fetchUnits();
//        for (int i = 0; i < units.size(); i++) {
//            Events et = new Events(DateUtil.getDisplayDate(units.get(i).getStaffDate()),
//                    units.get(i).getTitle());
//            eventsArrayList.add(et);
//        }
//        CustomCalendarView.createEvents(eventsArrayList, eventDisplayDate);
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
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);


//                unitBinding.tvStaffDate.setVisibility(View.GONE);
//                unitBinding.tvMyDate.setVisibility(View.GONE);
                if (DateUtil.getDisplayDate(model.getMyDate()).equals(eventDate.get())) {
//                    CustomCalendarView.createEvents(eventsArrayList);
                    emptyVisible.set(false);
                    unitBinding.unitCode.setText(model.getTitle());
                    unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                            + model.getUnitHour() + " hrs");
                    if (!model.getStatus().isEmpty()) {
                        if (model.getStaffDate()>0) {
                            unitBinding.tvStaffDate.setText(model.getStatus() + " : " + DateUtil.getDisplayDate(model.getStatusDate()));
                            unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                        }
                    }else {
                        unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                    }
                    unitBinding.tvDescription.setText(model.getDesc());
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                        if (model.getComment() != null) {
                            unitBinding.tvComment.setText(model.getComment());
                        } else {
                            unitBinding.tvComment.setVisibility(View.GONE);
                        }
                    }else {
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
                    }else {
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
                } else {
                    emptyVisible.set(true);
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
            }
        }
    }


    public static class CustomCalendarAdapter extends ArrayAdapter {

        List<Date> dates;
        Calendar currentDate;
        List<Events> events;
        LayoutInflater inflater;
        Context context;
        public int selectedPosition;


        public CustomCalendarAdapter(@androidx.annotation.NonNull Context context,
                                     List<Date> dates, Calendar currentDate, List<Events> events) {
            super(context, R.layout.t_row_calender_view);
            this.context = context;
            this.dates = dates;
            this.currentDate = currentDate;
            this.events = events;
            inflater = LayoutInflater.from(context);
        }

//        @androidx.annotation.Nullable
//        @Override
//        public Object getItem(int position) {
//            return super.getItem(position);
//        }

        @androidx.annotation.NonNull
        @Override
        public View getView(int position, @androidx.annotation.Nullable View convertView,
                            @androidx.annotation.NonNull ViewGroup parent) {
            Date monthdate = dates.get(position);
            View view = convertView;
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(monthdate);
            int dayNo = dateCalendar.get(Calendar.DATE);
            int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
            int displayYear = dateCalendar.get(Calendar.YEAR);
            int currentMonth = currentDate.get(Calendar.MONTH) + 1;
            int currentYear = currentDate.get(Calendar.YEAR);

                startDateTime = dates.get(0).getTime();
             endDateTime = dates.get(41).getTime();


            if (view == null) {
                boolean tabletSize = context.getResources().getBoolean(R.bool.isTablet);
                if (tabletSize) {
                    view = inflater.inflate(R.layout.t_row_cal_tabview, parent, false);
                }else {
                    view = inflater.inflate(R.layout.t_row_calender_view, parent, false);
                }
            }
            Calendar eventCalendar = Calendar.getInstance();

            TextView day = view.findViewById(R.id.cal_day);
            TextView eventText = view.findViewById(R.id.day_event);
            TextView eventText1 = view.findViewById(R.id.day_event1);
            TextView eventText2 = view.findViewById(R.id.day_event2);
            View event = view.findViewById(R.id.event_id);
            day.setText(String.valueOf(dayNo));

            if (setSelected.get()) {

                if (selectedPosition == position && displayMonth == currentMonth) {
                    day.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_light_blue));
                } else {
                    day.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                }
            } else {
                if (dayNo == eventCalendar.get(Calendar.DAY_OF_MONTH) && currentMonth == eventCalendar.get(Calendar.MONTH) + 1
                        && displayYear == eventCalendar.get(Calendar.YEAR) && displayMonth== currentMonth) {
                    eventDate.set(DateUtil.getDisplayDate(dates.get(position).getTime()));
                    dispDate.set(DateUtil.getCalendarDate(dates.get(position).getTime()));
                    if (displayYear == currentYear) {
                        day.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_all_blue));
                        day.setTextColor(ContextCompat.getColor(context, R.color.white));
                    }
                    unitsAdapter.setItems(units);
                    unitsAdapter.notifyDataSetChanged();
                }
            }
            if (dayNo == eventCalendar.get(Calendar.DAY_OF_MONTH) && currentMonth == eventCalendar.get(Calendar.MONTH) + 1
                    && displayYear == eventCalendar.get(Calendar.YEAR)) {
//                eventDate.set(DateUtil.getDisplayDate(dates.get(position).getTime()));
//                dispDate.set(DateUtil.getCalendarDate(dates.get(position).getTime()));
                if (displayMonth == currentMonth && displayYear == currentYear) {
                    day.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_all_blue));
                    day.setTextColor(ContextCompat.getColor(context, R.color.white));
                }
            }
            if (displayMonth == currentMonth && displayYear == currentYear) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            } else {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                day.setTextColor(ContextCompat.getColor(context, R.color.gray_3));
            }

            List<Events> mevents=getEvents(dayNo,displayMonth,displayYear);

            if(mevents.size()>0) {
                eventText.setVisibility(View.VISIBLE);
                eventText.setText(mevents.get(0).getTitle());
                if (mevents.get(0).getType().matches("Course")){
                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
//                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
//                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
                }
                else if (mevents.get(0).getType().matches("Experience")){
                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
//                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
//                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
                }
                else if (mevents.get(0).getType().matches("Study Task")){
                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
//                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
//                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
                }
            }
            if (mevents.size()>1){
                eventText1.setVisibility(View.VISIBLE);
                eventText1.setText(mevents.get(1).getTitle());
                if (mevents.get(1).getType().equals("Course")){
//                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
//                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
                }
                 if (mevents.get(1).getType().equals("Experience")){
//                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
//                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
                }
                 if (mevents.get(1).getType().equals("Study Task")){
//                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
//                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
                }
            }
            if (mevents.size()>2){
                eventText2.setVisibility(View.VISIBLE);
                eventText2.setText(mevents.get(2).getTitle());
                if (mevents.get(2).getType().equals("Course")){
//                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
//                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
                }
                 if (mevents.get(2).getType().equals("Experience")){
//                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
//                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_experience));
                }
                 if (mevents.get(2).getType().equals("Study Task")){
//                    eventText.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
//                    eventText1.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_course));
                    eventText2.setBackgroundColor(ContextCompat.getColor(context, R.color.humana_study));
                }
            }



            view.setTag(position);
//            view.setOnClickListener(v -> {
//                AppCompatActivity activity = (AppCompatActivity) context;
//                eventDisplayDate = dates.get(position).getTime();
//                ActivityCalendarBottomSheet bottomSheetDialogFragment = new ActivityCalendarBottomSheet(eventDisplayDate);
//                bottomSheetDialogFragment.show(activity.getSupportFragmentManager(),
//                        "units");;
//                AppCompatActivity activity = (AppCompatActivity) context;
//                View dialogView = activity.getLayoutInflater().inflate(R.layout.frag_calendar_bottom_sheet, null);
//                BottomSheetDialog dialog = new BottomSheetDialog(activity);
//                dialog.setContentView(dialogView);
//                dialog.show();




//                Activity activity = (Activity) context;
//
//                Bundle b = new Bundle();
//                b.putLong("selectedDate", dates.get(position).getTime());
//                ActivityUtil.gotoPage(context, ActivityCalendarBottomSheet.class, b);
//                activity.overridePendingTransition( R.anim.slide_in_up, R.anim.slide_in_out );
//            });


            return view;
        }

        private List<Events> getEvents(int day,int displayMonth,int displayYear)
        {
            List<Events> mevents=new ArrayList<>();

            Calendar eventCalendar = Calendar.getInstance();
            for (int i = 0; i < events.size(); i++) {
                eventCalendar.setTime(convertStringToDate(events.get(i).getDATE()));

                if (day == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1
                        && displayYear == eventCalendar.get(Calendar.YEAR)) {
                    mevents.add(new Events(events.get(i).getDATE(), events.get(i).getTitle(), events.get(i).getType()));
                }
            }

            return mevents;
        }

        private Date convertStringToDate(String date) {
            Date date1 = null;
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            try {
                date1 = format.parse(date);
                System.out.println(date1);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return date1;
        }


        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public int getPosition(@androidx.annotation.Nullable Object item) {
            return dates.indexOf(item);
        }



    }
    
    }