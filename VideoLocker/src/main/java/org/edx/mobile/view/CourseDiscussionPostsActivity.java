package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

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

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;


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


    public void onClick(View v) {
        // TODO: pass topics instead of making the get topics API call again in DiscussionAddPostFragment
        Intent addPostIntent = new Intent(this, DiscussionAddPostActivity.class);
        addPostIntent.putExtra(DiscussionAddPostFragment.ENROLLMENT, courseData);
        addPostIntent.putExtra(DiscussionAddPostFragment.TOPIC, discussionTopic);
        addPostIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(addPostIntent);

        // For test purpose. TODO: put this behind the tap on response list when it's done.
//        Intent commentListIntent = new Intent(this, CourseDiscussionCommentsActivity.class);
//        commentListIntent.putExtra(Router.EXTRA_COURSE_DATA, courseData);
//        commentListIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        this.startActivity(commentListIntent);
    }
}
