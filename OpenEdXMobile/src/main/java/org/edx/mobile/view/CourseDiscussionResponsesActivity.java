package org.edx.mobile.view;

import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseDiscussionResponsesActivity extends BaseSingleFragmentActivity {

    @Inject
    CourseDiscussionResponsesFragment courseDiscussionResponsesFragment;

    @Override
    public Fragment getFirstFragment() {
        courseDiscussionResponsesFragment.setArguments(getIntent().getExtras());
        return courseDiscussionResponsesFragment;
    }
}
