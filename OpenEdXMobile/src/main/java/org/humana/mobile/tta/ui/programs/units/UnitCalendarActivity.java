package org.humana.mobile.tta.ui.programs.units;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.humana.mobile.R;
import org.humana.mobile.databinding.FragUnitCalendarViewBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.mxCalenderView.CustomCalendarView;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitCalendarViewModel;
import org.humana.mobile.view.Router;

public class UnitCalendarActivity extends BaseVMActivity {

    private UnitCalendarViewModel viewModel;

    private long periodId;
    private String periodName;
    private EnrolledCoursesResponse course;
    private BottomSheetBehavior behavior;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null){
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null){
            getBundledData(savedInstanceState);
        }

        viewModel = new UnitCalendarViewModel(this, course);
        ViewDataBinding viewDataBinding= binding(R.layout.frag_unit_calendar_view, viewModel);
        FragUnitCalendarViewBinding binding= (FragUnitCalendarViewBinding) viewDataBinding;
        setSupportActionBar(findViewById(R.id.toolbar));
        binding.calendarView.setCalendarListener(new CustomCalendarView.CalendarListener() {
            @Override
            public void onAction(long date) {
                viewModel.eventDisplayDate = date;
                viewModel.fetchUnits();
            }
        });
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
}
