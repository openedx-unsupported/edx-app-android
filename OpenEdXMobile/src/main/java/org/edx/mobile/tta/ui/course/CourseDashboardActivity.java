package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.view_model.CourseDashboardViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

public class CourseDashboardActivity extends BaseVMActivity {

    private Content content;
    private boolean isPush = false;
    private CourseDashboardViewModel viewModel;

    private Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        viewModel = new CourseDashboardViewModel(this, content);
        binding(R.layout.t_activity_course_dashboard, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey(Constants.KEY_IS_PUSH)){
                isPush = parameters.getBoolean(Constants.KEY_IS_PUSH);
            }
            if (parameters.containsKey(Constants.KEY_CONTENT)){
                content = parameters.getParcelable(Constants.KEY_CONTENT);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!isPush){
            super.onBackPressed();
        } else {
            ActivityUtil.gotoPage(this, LandingActivity.class);
            finish();
        }
    }
}
