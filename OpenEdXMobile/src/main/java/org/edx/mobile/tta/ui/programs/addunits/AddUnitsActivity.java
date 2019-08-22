package org.edx.mobile.tta.ui.programs.addunits;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.programs.addunits.viewmodel.AddUnitsViewModel;

public class AddUnitsActivity extends BaseVMActivity {

    private AddUnitsViewModel viewModel;

    private Period period;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null){
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null){
            getBundledData(savedInstanceState);
        }

        viewModel = new AddUnitsViewModel(this, period);
        binding(R.layout.t_activity_add_units, viewModel);

        setSupportActionBar(findViewById(R.id.toolbar));
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
    }

    private void getBundledData(Bundle parameters){
        if (parameters.containsKey(Constants.KEY_PERIOD)){
            period = parameters.getParcelable(Constants.KEY_PERIOD);
        }
    }
}
