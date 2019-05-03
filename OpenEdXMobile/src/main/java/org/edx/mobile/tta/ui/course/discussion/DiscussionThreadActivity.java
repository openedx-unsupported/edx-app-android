package org.edx.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.DiscussionTopicType;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.discussion.view_model.DiscussionThreadViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.view.common.PageViewStateCallback;

public class DiscussionThreadActivity extends BaseVMActivity {
    private int RANK;

    private DiscussionThreadViewModel viewModel;

    private EnrolledCoursesResponse course;
    private DiscussionTopic topic;
    private DiscussionThread thread;

    private Toolbar toolbar;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.thread.name()));
        getExtras();
        viewModel = new DiscussionThreadViewModel(this, course, topic, thread);
        viewModel.registerEventBus();
        binding(R.layout.t_activity_discussion_thread, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        analytic.addMxAnalytics_db(
                topic.getName().contains("लेखक") ?
                        DiscussionTopicType.Postname_AD.name() :
                        DiscussionTopicType.Postname_CD.name(),
                Action.DBView, course.getCourse().getName(),
                Source.Mobile, thread.getIdentifier());
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

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null){
            course = (EnrolledCoursesResponse) parameters.getSerializable(Constants.KEY_ENROLLED_COURSE);
            topic = (DiscussionTopic) parameters.getSerializable(Constants.KEY_DISCUSSION_TOPIC);
            thread = (DiscussionThread) parameters.getSerializable(Constants.KEY_DISCUSSION_THREAD);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.thread.name()));
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
