package org.edx.mobile.tta.ui.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.connect.view_model.ConnectDashboardViewModel;

public class ConnectDashboardActivity extends BaseVMActivity {

    private ConnectDashboardViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ConnectDashboardViewModel(this);
        binding(R.layout.t_activity_connect_dashboard, viewModel);
    }
}
