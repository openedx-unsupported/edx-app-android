package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.discussion.DiscussionComment;

import roboguice.inject.InjectExtra;

public class DiscussionAddResponseActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddResponseFragment discussionAddResponseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        discussionAddResponseFragment.setArguments(getIntent().getExtras());
        return discussionAddResponseFragment;
    }
}
