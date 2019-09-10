package org.edx.mobile.tta.ui.programs.pendingUnits;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.programs.pendingUnits.viewModel.PendingUnitsListViewModel;

public class PendingUnitsListActivity extends BaseVMActivity {
    private PendingUnitsListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new PendingUnitsListViewModel(this);
        binding(R.layout.t_activity_pending_units_list, viewModel);

        savedInstanceState = getIntent().getExtras();
        assert savedInstanceState != null;
        viewModel.userName = savedInstanceState.getString("username");

    }
}