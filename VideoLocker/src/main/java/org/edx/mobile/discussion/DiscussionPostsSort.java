package org.edx.mobile.discussion;

import android.support.annotation.StringRes;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.TextResourceProvider;

/**
 * Sort options for discussion posts.
 */
public enum DiscussionPostsSort implements TextResourceProvider {
    NONE(R.string.discussion_posts_sort_recent_activity),
    LAST_ACTIVITY_AT(R.string.discussion_posts_sort_most_activity),
    VOTE_COUNT(R.string.discussion_posts_sort_most_votes);

    @StringRes
    private final int textRes;

    DiscussionPostsSort(@StringRes int textRes) {
        this.textRes = textRes;
    }

    @Override
    public int getTextResource() {
        return textRes;
    }
}
