package org.edx.mobile.view;

import androidx.fragment.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

public class CourseDiscussionCommentsActivity extends BaseSingleFragmentActivity {

    @Inject
    private CourseDiscussionCommentsFragment commentsFragment;

    @Override
    public Fragment getFirstFragment() {
        commentsFragment.setArguments(getIntent().getExtras());
        commentsFragment.setRetainInstance(true);
        return commentsFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.discussion_comments));
    }

}
