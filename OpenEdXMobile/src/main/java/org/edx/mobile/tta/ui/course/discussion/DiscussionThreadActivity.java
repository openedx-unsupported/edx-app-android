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
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.discussion.view_model.DiscussionThreadViewModel;

public class DiscussionThreadActivity extends BaseVMActivity {

    private DiscussionThreadViewModel viewModel;

    private EnrolledCoursesResponse course;
    private DiscussionTopic topic;
    private DiscussionThread thread;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        viewModel = new DiscussionThreadViewModel(this, course, topic, thread);
        viewModel.registerEventBus();
        binding(R.layout.t_activity_discussion_thread, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
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
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
