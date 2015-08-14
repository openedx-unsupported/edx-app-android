package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.ThreadComments;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.DiscussionCommentsAdapter;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionCommentsFragment extends RoboFragment {

    @InjectView(R.id.discussion_comments_listview)
    ListView discussionCommentsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @Inject
    DiscussionAPI discussionAPI;

    @Inject
    DiscussionCommentsAdapter discussionCommentsAdapter;

    @Inject
    Router router;

    private static final Logger logger = new Logger(CourseDiscussionCommentsFragment.class.getName());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionCommentsListView.setAdapter(discussionCommentsAdapter);

        // TODO: replace this with getComments API
        new DiscussionAPI().searchThreadList(courseData.getCourse().getId(), "critic", new APICallback<TopicThreads>() {
            @Override
            public void success(TopicThreads threads) {
                new DiscussionAPI().getCommentList(threads.getResults().get(0).getIdentifier(), new APICallback<ThreadComments>() {
                    @Override
                    public void success(ThreadComments comments) {
                        discussionCommentsAdapter.setItems(comments.getResults().get(0).getChildren());
                    }

                    @Override
                    public void failure(Exception e) {
                    }
                });
            }

            @Override
            public void failure(Exception e) {
            }
        });
    }
}