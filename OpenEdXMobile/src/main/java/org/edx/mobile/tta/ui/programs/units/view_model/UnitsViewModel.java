package org.edx.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFilterDropDownBinding;
import org.edx.mobile.databinding.TRowTextBinding;
import org.edx.mobile.databinding.TRowUnitBinding;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.ShowIn;
import org.edx.mobile.tta.data.enums.UnitStatusType;
import org.edx.mobile.tta.data.enums.UserRole;
import org.edx.mobile.tta.data.local.db.table.Unit;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramFilterTag;
import org.edx.mobile.tta.data.model.program.ProgramUser;
import org.edx.mobile.tta.event.CourseEnrolledEvent;
import org.edx.mobile.tta.event.program.PeriodSavedEvent;
import org.edx.mobile.tta.event.program.ShowStudentUnitsEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;
import org.edx.mobile.tta.ui.mxCalenderView.CustomCalendarView;
import org.edx.mobile.tta.ui.mxCalenderView.Events;
import org.edx.mobile.tta.ui.programs.units.UnitCalendarActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


    private EnrolledCoursesResponse course;
    private List<Unit> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private EnrolledCoursesResponse parentCourse;

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
                        mDataManager.getLoginPrefs().getSectionId(), data, unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
                                unit.setMyDate(data);
                                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));
                                if (response.getSuccess()) {
                                    mActivity.showLongSnack("Proposed date set successfully");
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

    private void fetchFilters() {

        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.units.name(),
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {
                        if (!data.isEmpty()) {
                            allFilters = data;
                            filterSize = allFilters.size();
                            filtersVisible.set(true);
                            filtersAdapter.setItems(allFilters);
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

        mDataManager.getUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), "", mDataManager.getLoginPrefs().getRole(), 0L, take, skip,
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

                List<DropDownFilterView.FilterItem> items = new ArrayList<>();
                items.add(new DropDownFilterView.FilterItem(model.getDisplayName(), null,
                        true, R.color.primary_cyan, R.drawable.t_background_tag_hollow
                ));
                for (ProgramFilterTag tag : model.getTags()) {
                    items.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                            false, R.color.white, R.drawable.t_background_tag_filled
                    ));
                }
                dropDownBinding.filterDropDown.setFilterItems(items);

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
                    if (prev != null && prev.getItem() != null) {
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null) {
                        tags.add((ProgramFilterTag) item.getItem());
                    }

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
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
               /* if (selectedEvent.get() != null) {
                    if (selectedEvent.get()== String.valueOf(model.getMyDate())) {
                        unitBinding.setUnit(model);

                        unitBinding.unitCode.setText(model.getCode());
                        if (!TextUtils.isEmpty(model.getPeriodName())) {
                            unitBinding.unitCode.append("    |    " + model.getPeriodName());
                        }
                        unitBinding.layoutCheckbox.setVisibility(View.GONE);

                        if (model.getMyDate() > 0) {
                            unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                            Events e = new Events(DateUtil.getDisplayDate(model.getStaffDate()));
                            eventsArrayList.add(e);
                        } else {
                            unitBinding.tvMyDate.setText(R.string.proposed_date);
                        }

                        String role = mDataManager.getLoginPrefs().getRole();
                        if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                            if (model.getStaffDate() > 0) {
                                unitBinding.tvStaffDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                                unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                                Events e = new Events(DateUtil.getDisplayDate(model.getStaffDate()));
                                eventsArrayList.add(e);
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
                    }
                }else {*/
                    unitBinding.setUnit(model);

                    unitBinding.unitCode.setText(model.getCode());
                    if (!TextUtils.isEmpty(model.getPeriodName())) {
                        unitBinding.unitCode.append("    |    " + model.getPeriodName());
                    }
                    unitBinding.layoutCheckbox.setVisibility(View.GONE);

                    if (model.getMyDate() > 0) {
                        unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                        Events e = new Events(DateUtil.getDisplayDate(model.getStaffDate()));
                        eventsArrayList.add(e);
                    } else {
                        unitBinding.tvMyDate.setText(R.string.proposed_date);
                    }

                    String role = mDataManager.getLoginPrefs().getRole();
                    if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                        if (model.getStaffDate() > 0) {
                            unitBinding.tvStaffDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                            unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                            Events e = new Events(DateUtil.getDisplayDate(model.getStaffDate()));
                            eventsArrayList.add(e);
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
//                }

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
//        if (frameVisible.get()) {
//            frameVisible.set(false);
//            calVisible.set(true);
//            filtersVisible.set(false);
//            switchText.set("Grid View");
//        } else {
//            frameVisible.set(true);
//            calVisible.set(false);
//            filtersVisible.set(true);
//            switchText.set("Calender View");
//        }
        CustomCalendarView.createEvents(eventsArrayList);
        ActivityUtil.gotoPage(mActivity, UnitCalendarActivity.class);

//    FragmentTransaction fragmentTransaction = mFragment.getFragmentManager().beginTransaction();
//    fragmentTransaction.replace(R.id.fl_unit, new UnitCalenderFragment());
//    fragmentTransaction.commit();
    }

    public static class CustomCalendarAdapter extends ArrayAdapter {

        List<Date> dates;
        Calendar currentDate;
        List<Events> events;
        LayoutInflater inflater;
        Context context;


        public CustomCalendarAdapter(@androidx.annotation.NonNull Context context, List<Date> dates, Calendar currentDate, List<Events> events) {
            super(context, R.layout.t_row_calender_view);
            this.context = context;
            this.dates = dates;
            this.currentDate = currentDate;
            this.events = events;
            inflater = LayoutInflater.from(context);
        }

        @androidx.annotation.Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @androidx.annotation.NonNull
        @Override
        public View getView(int position, @androidx.annotation.Nullable View convertView, @androidx.annotation.NonNull ViewGroup parent) {
            Date monthdate = dates.get(position);
            View view = convertView;
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(monthdate);
            int dayNo = dateCalendar.get(Calendar.DAY_OF_MONTH);
            int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
            int displayYear = dateCalendar.get(Calendar.YEAR);
            int currentMonth = currentDate.get(Calendar.MONTH) + 1;
            int currentYear = currentDate.get(Calendar.YEAR);

            if (view == null){
                view = inflater.inflate(R.layout.t_row_calender_view, parent, false);

            }


            TextView day = view.findViewById(R.id.cal_day);
            View event = view.findViewById(R.id.event_id);
            day.setText(String.valueOf(dayNo));


            if (displayMonth == currentMonth && displayYear == currentYear){
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }else {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                day.setTextColor(ContextCompat.getColor(context, R.color.gray_3));
            }

            Calendar eventCalendar = Calendar.getInstance();
            ArrayList<String> arrayList = new ArrayList<>();

            for (int i = 0; i < events.size(); i++){
                eventCalendar.setTime(convertStringToDate(events.get(i).getDATE()));
                if (dayNo == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH)+1
                        && displayYear == eventCalendar.get(Calendar.YEAR)){
//                    arrayList.add(events.get(i).getEVENT());
                    event.setVisibility(View.VISIBLE);
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    frameVisible.set(true);
                    calVisible.set(false);
                    filtersVisible.set(true);
                    switchText.set("Calender View");
                    selectedEvent.set(dates.get(position).getDate());
                }
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

        private Date convertStringToDate(String date){
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
