package org.edx.mobile.discussion;

import androidx.annotation.StringRes;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.TextResourceProvider;

/**
 * Sort options for discussion posts.
 */
public enum DiscussionPostsSort implements TextResourceProvider {
    LAST_ACTIVITY_AT(R.string.discussion_posts_sort_recent_activity, "last_activity_at"),
    COMMENT_COUNT(R.string.discussion_posts_sort_most_activity, "comment_count"),
    VOTE_COUNT(R.string.discussion_posts_sort_most_votes, "vote_count");

    @StringRes
    private final int textRes;
    private final String queryParamValue;

    DiscussionPostsSort(@StringRes int textRes, String queryParamValue) {
        this.textRes = textRes;
        this.queryParamValue = queryParamValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTextResource() {
        return textRes;
    }

    /**
     * Get the value of the query parameter.
     *
     * @return The query parameter string
     */
    public String getQueryParamValue(){
        return queryParamValue;
    }
}
