package org.edx.mobile.tta.ui.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

public class SplashActivity extends BaseVMActivity {
    private static final int RANK = 0;

    private SplashViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SplashViewModel(this);
        binding(R.layout.t_activity_splash, viewModel);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.appopen.name()));
        viewModel.startRouting(this);
    }
}
