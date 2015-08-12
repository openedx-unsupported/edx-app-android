package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

import roboguice.inject.InjectExtra;

public class CourseDiscussionPostsActivity extends BaseSingleFragmentActivity  {

    @Inject
    private CourseDiscussionPostsThreadFragment courseDiscussionPostsThreadFragment;

    @Inject
    private CourseDiscussionPostsSearchFragment courseDiscussionPostsSearchFragment;

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        Fragment fragment = new Fragment();
        Bundle extras = new Bundle();

        if (searchQuery != null) {
            extras.putString(Router.EXTRA_SEARCH_QUERY, searchQuery);
            fragment = courseDiscussionPostsSearchFragment;
        }

        if (discussionTopic != null) {
            extras.putSerializable(Router.EXTRA_DISCUSSION_TOPIC, discussionTopic);
            fragment = courseDiscussionPostsThreadFragment;
        }

        fragment.setArguments(extras);
        fragment.setRetainInstance(true);

        return fragment;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (searchQuery != null) {
            setTitle(getString(R.string.discussion_posts_search_title));
            return;
        }

        if (discussionTopic != null && discussionTopic.getName() != null) {
            setTitle(discussionTopic.getName());
        }

    }
}
