package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;
import org.edx.mobile.discussion.DiscussionComment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

import roboguice.inject.InjectExtra;

public class DiscussionAddCommentActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddCommentFragment discussionAddCommentFragment;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_COMMENT, optional = true)
    DiscussionComment discussionComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTitle(getString(discussionComment == null ? R.string.discussion_response : R.string.discussion_comment));
    }

    @Override
    public Fragment getFirstFragment() {
        discussionAddCommentFragment.setArguments(getIntent().getExtras());
        return discussionAddCommentFragment;
    }
}
