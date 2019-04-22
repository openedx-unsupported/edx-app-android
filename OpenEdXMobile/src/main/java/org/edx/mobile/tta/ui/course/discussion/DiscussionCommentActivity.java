package org.edx.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.discussion.view_model.DiscussionCommentViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

public class DiscussionCommentActivity extends BaseVMActivity {
    private int RANK;

    private DiscussionCommentViewModel viewModel;

    private EnrolledCoursesResponse course;
    private DiscussionTopic topic;
    private DiscussionThread thread;
    private DiscussionComment comment;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        getExtras();
        viewModel = new DiscussionCommentViewModel(this, course, topic, thread, comment);
        binding(R.layout.t_activity_discussion_comment, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel.registerEventBus();
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
            comment = (DiscussionComment) parameters.getSerializable(Constants.KEY_DISCUSSION_COMMENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.comment.name()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
