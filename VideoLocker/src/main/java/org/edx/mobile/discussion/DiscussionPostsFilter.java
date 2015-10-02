package org.edx.mobile.discussion;

import android.support.annotation.StringRes;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.TextResourceProvider;

/**
 * Filter options for discussion posts.
 */
public enum DiscussionPostsFilter implements TextResourceProvider {
    ALL(R.string.discussion_posts_filter_all_posts),
    UNREAD(R.string.discussion_posts_filter_unread_posts),
    UNANSWERED(R.string.discussion_posts_filter_unanswered_posts);

    @StringRes
    private final int textRes;

    DiscussionPostsFilter(@StringRes int textRes) {
        this.textRes = textRes;
    }

    @Override
    public int getTextResource() {
        return textRes;
    }
}
