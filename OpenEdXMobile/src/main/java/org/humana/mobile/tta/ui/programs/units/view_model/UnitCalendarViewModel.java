package org.humana.mobile.tta.ui.programs.units.view_model;

import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
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
    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

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
    public static ObservableField<String> SelectedCurrentDate = new ObservableField<>();


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
//        unitsAdapter.setItems(units);
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
                                    eventsArrayList.clear();
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
    }

    private void fetchUnits() {

        mDataManager.getUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), "", mDataManager.getLoginPrefs().getRole(), 0L, take, skip,
                new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        units = data;
                        populateUnits(data);
                        eventsArrayList.clear();
                        for (int i = 0; i < data.size(); i++) {
                            Events et = new Events(DateUtil.getDisplayDate(data.get(i).getStaffDate()),
                                    data.get(i).getTitle());
                            eventsArrayList.add(et);
                        }
                        CustomCalendarView.createEvents(eventsArrayList);
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

    public void onEventMainThread(List<Unit> unit) {
//        filters.clear();
        changesMade = true;
        allLoaded = false;
        fetchData();
//        ProgramFilter pf = new ProgramFilter();
//        pf.setDisplayName(user.username);
//        pf.setInternalName(user.name);
//        pf.setId(user.name);
//        pf.setOrder(user.completedHours);
//        pf.setShowIn(new ArrayList<String>());
//        pf.setTags(tags);
//        allFilters.add(pf);
//        filtersAdapter.notifyItemChanged(3, 4);


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
                    unitBinding.unitCode.setText(model.getCode());
                    if (!TextUtils.isEmpty(model.getPeriodName())) {
                        unitBinding.unitCode.append("    |    " + model.getPeriodName());
                    }
                    unitBinding.layoutCheckbox.setVisibility(View.GONE);

                    if (model.getMyDate() > 0) {
                        unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));

                    } else {
                        unitBinding.tvMyDate.setText(R.string.proposed_date);
                    }

                    String role = mDataManager.getLoginPrefs().getRole();
                    if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                        if (model.getStaffDate() > 0) {
                            unitBinding.tvStaffDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                            unitBinding.tvStaffDate.setVisibility(View.VISIBLE);

                        } else {
                            unitBinding.tvStaffDate.setVisibility(View.GONE);
                        }
                    }

                    if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name()) &&
                            !TextUtils.isEmpty(model.getStatus())) {
                        try {
                            switch (UnitStatusType.valueOf(model.getStatus())) {
                                case Completed:
                                    unitBinding.statusIcon.setImageDrawable(
                                            ContextCompat.getDrawable(getContext(), R.drawable.t_icon_done));
                                    unitBinding.statusIcon.setVisibility(View.VISIBLE);
                                    break;
                                case InProgress:
                                    unitBinding.statusIcon.setImageDrawable(
                                            ContextCompat.getDrawable(getContext(), R.drawable.t_icon_refresh));
                                    unitBinding.statusIcon.setVisibility(View.VISIBLE);
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



//    public class CustomCalendarView extends LinearLayout {
//        ImageButton nextButton, previousButton;
//        TextView currentDate;
//        GridView calGrid;
//        private static final int MAX_CALENDAR_DAYS = 42;
//        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
//        Context context;
//        UnitCalendarViewModel.CustomCalendarAdapter adapter;
//
//
//        List<Date> dates = new ArrayList<>();
//        List<Events> eventsList = new ArrayList<>();
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM/YYYY", Locale.ENGLISH);
//        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
//        SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY", Locale.ENGLISH);
//
//
//        public CustomCalendarView(Context context) {
//            super(context);
//            this.context = context;
////        Log.d("CustomCalendarView", "CustomCalendarView: 2");
////        createEvents(eventsList);
//            initializeView();
////        setCustomAdapter();
//            setUpCalendar();
//
//            previousButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    calendar.add(Calendar.MONTH, -1);
//                    setUpCalendar();
//                }
//            });
//            nextButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    calendar.add(Calendar.MONTH, 1);
//                    setUpCalendar();
//                }
//            });
//        }
//
//
//        private void initializeView() {
//////        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View view = LayoutInflater.from(context).inflate(R.layout.mx_custom_calender_view, this, true);
//
//            nextButton = view.findViewById(R.id.ib_next);
//            previousButton = view.findViewById(R.id.ib_prev);
//            currentDate = view.findViewById(R.id.tv_date);
//            calGrid = view.findViewById(R.id.cal_grid);
//        }
//
//
//        public void setUpCalendar() {
//            String date = simpleDateFormat.format(calendar.getTime());
//            currentDate.setText(date);
//            dates.clear();
//
//            Calendar monthCalendar = (Calendar) calendar.clone();
//            monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
//
//            int FIRST_DAY_OF_MONTH = monthCalendar.get(Calendar.DAY_OF_WEEK_IN_MONTH)  -1;
//
//            monthCalendar.add(Calendar.DAY_OF_MONTH, -FIRST_DAY_OF_MONTH);
//
//            while (dates.size() < MAX_CALENDAR_DAYS) {
//                dates.add(monthCalendar.getTime());
//                monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
//
//            }
//
//            adapter = new CustomCalendarAdapter(context, dates, calendar, eventsList);
//            calGrid.setAdapter(adapter);
//            adapter.notifyDataSetChanged();
//
//        }
//
//        public void createEvents(List<Events> events) {
//            Events e;
//            eventsList.clear();
//            for (int i = 0; i < events.size(); i++) {
//                e = new Events(events.get(i).getDATE(), events.get(i).getEventText());
//                eventsList.add(e);
//            }
//            setUpCalendar();
////        adapter.notifyDataSetChanged();
//        }
//
//    }



    public static class CustomCalendarAdapter extends ArrayAdapter {

        List<Date> dates;
        Calendar currentDate;
        List<Events> events;
        LayoutInflater inflater;
        Context context;
        public int selectedPosition;


        public CustomCalendarAdapter(@androidx.annotation.NonNull Context context, List<Date> dates, Calendar currentDate, List<Events> events) {
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
        public View getView(int position, @androidx.annotation.Nullable View convertView, @androidx.annotation.NonNull ViewGroup parent) {
            Date monthdate = dates.get(position);
            View view = convertView;
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(monthdate);
            int dayNo = dateCalendar.get(Calendar.DATE);
            int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
            int displayYear = dateCalendar.get(Calendar.YEAR);
            int currentMonth = currentDate.get(Calendar.MONTH) + 1;
            int currentYear = currentDate.get(Calendar.YEAR);


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

            for (int i = 0; i < events.size(); i++) {
                eventCalendar.setTime(convertStringToDate(events.get(i).getDATE()));
                if (dayNo == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1
                        && displayYear == eventCalendar.get(Calendar.YEAR)) {
//                    event.setVisibility(View.VISIBLE);
                    eventText.setText(events.get(i).getEventText());
                    eventText.setVisibility(View.VISIBLE);
                }
            }
            view.setTag(position);
            view.setOnClickListener(v -> {
                ActivityUtil.gotoPage(context, ActivityCalendarBottomSheet.class);
//                selectedPosition = (int) v.getTag();
//                eventDate.set(DateUtil.getDisplayDate(dates.get(position).getTime()));
//                dispDate.set(DateUtil.getCalendarDate(dates.get(position).getTime()));
//                setSelected.set(true);
//                List<Unit> filterUnits = new ArrayList<>();
//                for (int i = 0; i < units.size(); i++) {
//                    if (DateUtil.getDisplayDate(units.get(i).getMyDate()).equals(eventDate.get())) {
//                        filterUnits.add(units.get(i));
////                        unitsAdapter.setItems(filterUnits);
////                        unitsAdapter.notifyDataSetChanged();
//                        emptyVisible.set(false);
//                    }
//                }
//                unitsAdapter.setItems(filterUnits);
//                unitsAdapter.notifyDataSetChanged();
//                if (filterUnits.size() == 0) {
//                    emptyVisible.set(true);
//                }
//
//
//                notifyDataSetChanged();
            });


            return view;
        }

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public int getPosition(@androidx.annotation.Nullable Object item) {
            return dates.indexOf(item);
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

    }

}
