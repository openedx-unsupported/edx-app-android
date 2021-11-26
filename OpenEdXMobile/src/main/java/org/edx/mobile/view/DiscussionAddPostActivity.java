package org.edx.mobile.view;

import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiscussionAddPostActivity extends BaseSingleFragmentActivity {

    @Inject
    DiscussionAddPostFragment discussionAddPostFragment;

    @Override
    public Fragment getFirstFragment() {
        discussionAddPostFragment.setArguments(getIntent().getExtras());
        return discussionAddPostFragment;
    }
}
