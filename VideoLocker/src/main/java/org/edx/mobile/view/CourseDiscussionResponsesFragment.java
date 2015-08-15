package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Releasable;
import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;
import com.qualcomm.qlearn.sdk.discussion.ThreadComments;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionResponsesFragment extends RoboFragment {

    @Inject
    LinearLayoutManager linearLayoutManager;

    @InjectView(R.id.discussion_responses_recycler_view)
    RecyclerView discussionResponsesRecyclerView;

    @InjectView(R.id.create_new_item_text_view)
    TextView addResponseTextView;

    @InjectView(R.id.create_new_item_relative_layout)
    RelativeLayout addResponseLayout;

    @InjectExtra(Router.EXTRA_DISCUSSION_THREAD)
    DiscussionThread discussionThread;

    @InjectExtra(value = Router.EXTRA_COURSE_DATA, optional = true)
    EnrolledCoursesResponse courseData;

    @Inject
    CourseDiscussionResponsesAdapter courseDiscussionResponsesAdapter;

    @Inject
    DiscussionAPI discussionAPI;

    @Inject
    Router router;

    private final Logger logger = new Logger(getClass().getName());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionResponsesRecyclerView.setLayoutManager(linearLayoutManager);

        courseDiscussionResponsesAdapter.setDiscussionThread(discussionThread);
        discussionResponsesRecyclerView.setAdapter(courseDiscussionResponsesAdapter);
        discussionAPI.getCommentList(discussionThread.getIdentifier(), new APICallback<ThreadComments>() {
            @Override
            public void success(ThreadComments threadComments) {
                courseDiscussionResponsesAdapter.setDiscussionResponses(threadComments.getResults());
            }

            @Override
            public void failure(Exception e) {
                // TODO: Handle failure condition
            }
        });

        addResponseTextView.setText(R.string.discussion_responses_add_response);

        addResponseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionAddResponseOrComment(getActivity(), discussionThread.getTopicId(), null);
            }
        });
    }

}
