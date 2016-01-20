package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.discussion.DiscussionTopic;
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

        if (searchQuery != null) {
            fragment = courseDiscussionPostsSearchFragment;
        }

        if (discussionTopic != null) {
            fragment = courseDiscussionPostsThreadFragment;
        }
        fragment.setArguments(getIntent().getExtras());
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
            if (discussionTopic.getIdentifier().equals(DiscussionTopic.FOLLOWING_TOPICS_ID)) {
                SpannableString title = new SpannableString("   " + discussionTopic.getName());
                IconDrawable starIcon = new IconDrawable(this, FontAwesomeIcons.fa_star)
                        .colorRes(this, R.color.edx_grayscale_neutral_white_t)
                        .sizeRes(this, R.dimen.edx_base);
                starIcon.setBounds(0, 0, starIcon.getIntrinsicWidth(), starIcon.getIntrinsicHeight());
                starIcon.setTintList(null);
                ImageSpan iSpan = new ImageSpan(starIcon, ImageSpan.ALIGN_BASELINE);
                title.setSpan(iSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                setTitle(title);
            }
            else {
                setTitle(discussionTopic.getName());
            }
        }
    }

}
