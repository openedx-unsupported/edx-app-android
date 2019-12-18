package org.humana.mobile.tta.ui.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.FirebaseApp;

import org.humana.mobile.R;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.tta.analytics.analytics_enums.Nav;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.utils.BreadcrumbUtil;

public class SplashActivity extends BaseVMActivity {
    private static final int RANK = 0;

    private SplashViewModel viewModel;
    private LoginPrefs loginPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FirebaseApp.initializeApp(this);
        viewModel = new SplashViewModel(this);
        binding(R.layout.t_activity_splash, viewModel);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.appopen.name()));
        loginPrefs = new LoginPrefs(this);
        loginPrefs.clearCachedFilter();
        viewModel.startRouting(this);
    }
}
