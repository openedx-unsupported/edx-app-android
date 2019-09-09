package org.edx.mobile.tta.ui.programs.periodunits;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.programs.periodunits.viewmodel.PeriodUnitsViewModel;
import org.edx.mobile.view.Router;

public class PeriodUnitsActivity extends BaseVMActivity {

    private PeriodUnitsViewModel viewModel;

    private Period period;
    private EnrolledCoursesResponse course;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null){
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null){
            getBundledData(savedInstanceState);
        }

        viewModel = new PeriodUnitsViewModel(this, period, course);
        binding(R.layout.t_activity_period_units, viewModel);

        setSupportActionBar(findViewById(R.id.toolbar));
        viewModel.registerEventBus();
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
        if (period != null){
            outState.putParcelable(Constants.KEY_PERIOD, period);
        }
        if (course != null){
            outState.putSerializable(Router.EXTRA_COURSE_DATA, course);
        }
    }

    private void getBundledData(Bundle parameters){
        if (parameters.containsKey(Constants.KEY_PERIOD)){
            period = parameters.getParcelable(Constants.KEY_PERIOD);
        }
        if (parameters.containsKey(Router.EXTRA_COURSE_DATA)){
            course = (EnrolledCoursesResponse) parameters.getSerializable(Router.EXTRA_COURSE_DATA);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
