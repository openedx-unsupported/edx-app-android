package org.edx.mobile.view;

import androidx.fragment.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseDiscussionCommentsActivity extends BaseSingleFragmentActivity {

    @Inject
    CourseDiscussionCommentsFragment commentsFragment;

    @Override
    public Fragment getFirstFragment() {
        commentsFragment.setArguments(getIntent().getExtras());
        return commentsFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.discussion_comments));
    }
}
