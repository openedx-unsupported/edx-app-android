package org.humana.mobile.tta.ui.programs.units;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.lib.mxcalendar.models.Event;
import com.lib.mxcalendar.util.Builder;
import com.lib.mxcalendar.view.IMxCalenderListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.FragUnitCalendarViewBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitCalendarViewModel;
import org.humana.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UnitCalendarActivity extends BaseVMActivity implements IMxCalenderListener {

    public UnitCalendarViewModel viewModel;

    private long periodId;
    private String periodName;
    private EnrolledCoursesResponse course;
    private BottomSheetBehavior behavior;
    private FrameLayout bottom_sheet;
    private List<Event> eventList;
    FragUnitCalendarViewBinding binding;
    private Long currentDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null){
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null) {
            getBundledData(savedInstanceState);
        }
        viewModel = new UnitCalendarViewModel(this, course);
        eventList = new ArrayList<>();
        ViewDataBinding viewDataBinding= binding(R.layout.frag_unit_calendar_view, viewModel);
        binding= (FragUnitCalendarViewBinding) viewDataBinding;
        setSupportActionBar(findViewById(R.id.toolbar));

        binding.calendarView.init(
                new Builder()
        .setDayNameColor(null)
        .setHeaderColor(null)
        .setDayNumberColor(null)
        .setListner(this)
        .setTabletMode(isTabView()));


    }

    private Boolean isTabView() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.KEY_PERIOD_ID, periodId);
        if (periodName != null){
            outState.putString(Constants.KEY_PERIOD_NAME, periodName);
        }
        if (course != null){
            outState.putSerializable(Router.EXTRA_COURSE_DATA, course);
        }
    }

    private void getBundledData(Bundle parameters){
        if (parameters.containsKey(Constants.KEY_PERIOD_ID)){
            periodId = parameters.getLong(Constants.KEY_PERIOD_ID);
        }
        if (parameters.containsKey(Constants.KEY_PERIOD_NAME)){
            periodName = parameters.getString(Constants.KEY_PERIOD_NAME);
        }
        if (parameters.containsKey(Router.EXTRA_COURSE_DATA)){
            course = (EnrolledCoursesResponse) parameters.getSerializable(Router.EXTRA_COURSE_DATA);
        }
    }
    public void onEventMainThread(List<Event> events) {
        eventList = events;
        viewModel.eventObservable.set(events);
        viewModel.eventObservableDate.set(viewModel.startDateTime);
        binding.calendarView.setEvents(eventList,viewModel.startDateTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerEventBus();
    }

    @Override
    protected void onPause() {
        unRegisterEventBus();
        super.onPause();
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAction(long date, long startDateTime, long endDateTime) {
//        viewModel.startDateTime = startDateTime;
//        viewModel.endDateTime = endDateTime;
        viewModel.eventObservableDate.set(startDateTime);
        viewModel.eventObservable.set(eventList);
        viewModel.fetchUnits(startDateTime, endDateTime);
    }

    @Override
    public void onItemClick(Long selectedDate, Long startDateTime, Long endDateTime) {

        ActivityCalendarBottomSheet bottomSheetDialogFragment =
                new ActivityCalendarBottomSheet(selectedDate, startDateTime, endDateTime);
        bottomSheetDialogFragment.show(this.getSupportFragmentManager(),
                "units");
    }


}
