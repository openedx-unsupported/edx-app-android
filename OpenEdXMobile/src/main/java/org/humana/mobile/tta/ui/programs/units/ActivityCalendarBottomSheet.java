package org.humana.mobile.tta.ui.programs.units;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TFragmentUnitsBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.programs.units.view_model.CalendarBottomSheetViewModel;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitCalendarViewModel;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;
import org.humana.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

public class ActivityCalendarBottomSheet extends BaseVMActivity {

    private CalendarBottomSheetViewModel viewModel;

    private long periodId;
    private String periodName;
    private EnrolledCoursesResponse course;
    private BottomSheetBehavior behavior;
    private Long selectedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null){
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null){
            getBundledData(savedInstanceState);
        }
        savedInstanceState = getIntent().getExtras();
        selectedDate = savedInstanceState.getLong("selectedDate", 0L);

        viewModel = new CalendarBottomSheetViewModel(this, course, selectedDate);
        binding(R.layout.frag_calendar_bottom_sheet, viewModel);

        setSupportActionBar(findViewById(R.id.toolbar));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
//                Intent in = new Intent(ActivityCalendarBottomSheet.this, UnitCalendarActivity.class);
//                startActivity(in);
//                finish();
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
