package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.view_model.CourseDashboardViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.view.common.PageViewStateCallback;

public class CourseDashboardActivity extends BaseVMActivity {
    private int RANK;

    private Content content;
    private boolean isPush = false;
    private int tabPosition;
    private CourseDashboardViewModel viewModel;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.course.name()));
        getExtras();
        viewModel = new CourseDashboardViewModel(this, content, tabPosition);
        binding(R.layout.t_activity_course_dashboard, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);

        viewModel.registerEventBus();

        analytic.addMxAnalytics_db(content.getName(), Action.CourseView, content.getName(),
                Source.Mobile, content.getSource_identity());
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
            if (parameters.containsKey(Constants.KEY_TAB_POSITION)){
                tabPosition = parameters.getInt(Constants.KEY_TAB_POSITION);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect_dashboard_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                viewModel.openShareMenu(findViewById(R.id.action_share));
                return true;
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

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.course.name()));
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
