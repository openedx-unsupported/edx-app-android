package org.edx.mobile.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import androidx.fragment.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.discussion.DiscussionTopic;
import org.edx.mobile.util.UiUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseDiscussionPostsActivity extends BaseSingleFragmentActivity {

    @Inject
    CourseDiscussionPostsThreadFragment courseDiscussionPostsThreadFragment;

    @Inject
    CourseDiscussionPostsSearchFragment courseDiscussionPostsSearchFragment;

    private String searchQuery;
    private DiscussionTopic discussionTopic;
    private EnrolledCoursesResponse courseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseExtras();
    }

    private void parseExtras() {
        searchQuery = getIntent().getStringExtra(Router.EXTRA_SEARCH_QUERY);
        discussionTopic = (DiscussionTopic) getIntent().getSerializableExtra(Router.EXTRA_DISCUSSION_TOPIC);
        courseData = (EnrolledCoursesResponse) getIntent().getSerializableExtra(Router.EXTRA_COURSE_DATA);
    }

    @Override
    public Fragment getFirstFragment() {
        final Fragment fragment;
        if (searchQuery != null) {
            fragment = courseDiscussionPostsSearchFragment;
        } else {
            fragment = courseDiscussionPostsThreadFragment;
        }

        // TODO: Move argument setting logic to base class
        if (fragment.getArguments() == null) {
            final Bundle args = new Bundle();
            args.putAll(getIntent().getExtras());
            args.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
            args.putBoolean(CourseDiscussionPostsThreadFragment.ARG_DISCUSSION_HAS_TOPIC_NAME,
                    discussionTopic != null);
            fragment.setArguments(args);
        }

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
            if (discussionTopic.isFollowingType()) {
                SpannableString title = new SpannableString("   " + discussionTopic.getName());
                Drawable starIcon = UiUtils.INSTANCE.getDrawable(this, R.drawable.ic_star_rate, R.dimen.edx_base);
                ImageSpan iSpan = new ImageSpan(starIcon, ImageSpan.ALIGN_BASELINE);
                title.setSpan(iSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                setTitle(title);
            } else {
                setTitle(discussionTopic.getName());
            }
        }
    }
}
