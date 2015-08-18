package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

public class CourseDiscussionResponsesActivity extends BaseSingleFragmentActivity {

    @Inject
    CourseDiscussionResponsesFragment courseDiscussionResponsesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        courseDiscussionResponsesFragment.setArguments(getIntent().getExtras());
        courseDiscussionResponsesFragment.setRetainInstance(true);

        return courseDiscussionResponsesFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTitle(R.string.course_discussion_responses_title);
    }
}
