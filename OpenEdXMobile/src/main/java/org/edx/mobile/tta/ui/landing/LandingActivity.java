package org.edx.mobile.tta.ui.landing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.CourseDashboardFragment;
import org.edx.mobile.tta.ui.landing.view_model.LandingViewModel;
import org.edx.mobile.tta.ui.search.SearchFragment;
import org.edx.mobile.tta.utils.ActivityUtil;

public class LandingActivity extends BaseVMActivity {

    private LandingViewModel viewModel;

    private boolean isPush = false;
    private Content content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LandingViewModel(this);
        binding(R.layout.t_activity_landing, viewModel);

        BottomNavigationView view = findViewById(R.id.dashboard_bottom_nav);
        view.setItemIconTintList(null);

        Bundle parameters = getIntent().getExtras();
        if (parameters != null){
            if (parameters.containsKey(Constants.KEY_IS_PUSH)){
                isPush = parameters.getBoolean(Constants.KEY_IS_PUSH);
            }
            if (parameters.containsKey(Constants.KEY_CONTENT)){
                content = parameters.getParcelable(Constants.KEY_CONTENT);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPush && content != null){
            ActivityUtil.replaceFragmentInActivity(
                    getSupportFragmentManager(),
                    CourseDashboardFragment.newInstance(content),
                    R.id.dashboard_fragment,
                    CourseDashboardFragment.TAG,
                    true,
                    null
            );
        }
    }

    @Override
    public void onBackPressed() {

        /*if (isPush){
            CourseDashboardFragment courseFragment = (CourseDashboardFragment) getSupportFragmentManager()
                    .findFragmentByTag(CourseDashboardFragment.TAG);
            if (courseFragment != null && courseFragment.isVisible()){

            }
        }*/
        isPush = false;

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.TAG);
        if (searchFragment != null && searchFragment.isVisible()){
            viewModel.selectLibrary();
        }
        super.onBackPressed();
    }
}
