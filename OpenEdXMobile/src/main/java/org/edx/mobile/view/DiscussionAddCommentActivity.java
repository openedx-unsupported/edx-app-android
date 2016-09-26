package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseSingleFragmentActivity;

public class DiscussionAddCommentActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddCommentFragment discussionAddCommentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        discussionAddCommentFragment.setArguments(getIntent().getExtras());
        return discussionAddCommentFragment;
    }
}
