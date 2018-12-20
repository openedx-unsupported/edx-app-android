package org.edx.mobile.tta.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.dashboard.view_model.DashboardViewModel;

public class DashboardActivity extends BaseVMActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding(R.layout.t_activity_dashboard, new DashboardViewModel(this));
    }
}
