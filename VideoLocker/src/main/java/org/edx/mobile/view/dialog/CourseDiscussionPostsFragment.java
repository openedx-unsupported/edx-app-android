package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionPostsFragment extends RoboFragment {

    @InjectView(R.id.discussion_posts_listview)
    private ListView discussionPostsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    DiscussionAPI discussionAPI;

    private final Logger logger = new Logger(getClass().getName());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionPostsListView.setAdapter(discussionPostsAdapter);

        if (searchQuery != null) {
            setIsFilterSortVisible(false);
            populateListFromSearch();
        }

        if (discussionTopic != null) {
            setIsFilterSortVisible(true);
            populateListFromThread();
        }

    }

    private void populateListFromThread() {
        // TODO: Remove hardcoded filter and sort
        discussionAPI.getThreadList(courseData.getCourse().getId(), discussionTopic.getIdentifier(),
                DiscussionAPI.DiscussionPostsFilter.All,
                DiscussionAPI.DiscussionPostsSort.None,
                new APICallback<TopicThreads>() {
                    @Override
                    public void success(TopicThreads topicThreads) {
                        discussionPostsAdapter.setItems(topicThreads.getResults());
                    }

                    @Override
                    public void failure(Exception e) {
                        // TODO: Handle failure gracefully
                    }
                });
    }

    private void populateListFromSearch() {
        discussionAPI.searchThreadList(courseData.getCourse().getId(), searchQuery,
                new APICallback<TopicThreads>() {
                    @Override
                    public void success(TopicThreads topicThreads) {
                        discussionPostsAdapter.setItems(topicThreads.getResults());
                    }

                    @Override
                    public void failure(Exception e) {

                    }
                });
    }

    private void setIsFilterSortVisible(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.INVISIBLE;
        // TODO: Set visibility of filter and sort
    }
}
