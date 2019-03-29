package org.edx.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

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
        binding(R.layout.t_activity_discussion_thread, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null){
            course = parameters.getParcelable(Constants.KEY_ENROLLED_COURSE);
            topic = parameters.getParcelable(Constants.KEY_DISCUSSION_TOPIC);
            thread = parameters.getParcelable(Constants.KEY_DISCUSSION_THREAD);
        }
    }
}
