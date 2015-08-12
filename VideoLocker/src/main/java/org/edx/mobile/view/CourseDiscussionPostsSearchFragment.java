package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.R;

import roboguice.inject.InjectExtra;

public class CourseDiscussionPostsSearchFragment extends CourseDiscussionPostsBaseFragment {

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_search_posts, container, false);
    }

    @Override
    protected void populateThreadList() {
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
}
