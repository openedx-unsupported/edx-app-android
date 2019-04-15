package org.edx.mobile.tta.ui.landing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.landing.view_model.LandingViewModel;
import org.edx.mobile.tta.ui.search.SearchFragment;

public class LandingActivity extends BaseVMActivity {

    private LandingViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LandingViewModel(this);
        binding(R.layout.t_activity_landing, viewModel);

        BottomNavigationView view = findViewById(R.id.dashboard_bottom_nav);
        view.setItemIconTintList(null);

        viewModel.registerEventBus();
    }

    @Override
    public void onBackPressed() {

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.TAG);
        if (searchFragment != null && searchFragment.isVisible()){
            viewModel.selectLibrary();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
