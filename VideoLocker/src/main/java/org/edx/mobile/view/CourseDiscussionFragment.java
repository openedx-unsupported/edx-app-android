package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.CourseTopics;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopicDepth;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.DiscussionTopicsAdapter;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionFragment extends RoboFragment {

    @InjectView(R.id.discussion_topics_listview)
    ListView discussion_topics_listview;

    @InjectExtra(Router.EXTRA_ENROLLMENT)
    private EnrolledCoursesResponse courseData;

    @Inject
    DiscussionAPI discussionAPI;

    @Inject
    DiscussionTopicsAdapter discussionTopicsAdapter;

    private static final Logger logger = new Logger(CourseDiscussionFragment.class.getName());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_topics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussion_topics_listview.setAdapter(discussionTopicsAdapter);

        discussionAPI.getTopicList(courseData.getCourse().getId(), new APICallback<CourseTopics>() {
            @Override
            public void success(CourseTopics courseTopics) {
                ArrayList<DiscussionTopic> allTopics = new ArrayList<DiscussionTopic>();
                allTopics.addAll(courseTopics.getCoursewareTopics());
                allTopics.addAll(courseTopics.getNonCoursewareTopics());

                List<DiscussionTopicDepth> allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                discussionTopicsAdapter.setItems(allTopicsWithDepth);
            }

            @Override
            public void failure(Exception e) {
                logger.error(e, false);
                // TODO: Handle error gracefully
            }
        });
    }

}
