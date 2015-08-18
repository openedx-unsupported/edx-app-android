package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import roboguice.inject.InjectExtra;

public class DiscussionAddPostActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddPostFragment discussionAddPostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTitle(getString(R.string.discussion_post));
    }

    @Override
    public Fragment getFirstFragment() {
        discussionAddPostFragment.setArguments(getIntent().getExtras());
        return discussionAddPostFragment;
    }
}
