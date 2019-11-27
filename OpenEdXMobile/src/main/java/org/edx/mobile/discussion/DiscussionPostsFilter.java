package org.edx.mobile.discussion;

import androidx.annotation.StringRes;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.TextResourceProvider;

/**
 * Filter options for discussion posts.
 */
public enum DiscussionPostsFilter implements TextResourceProvider {
    ALL(R.string.discussion_posts_filter_all_posts, ""),
    UNREAD(R.string.discussion_posts_filter_unread_posts, "unread"),
    UNANSWERED(R.string.discussion_posts_filter_unanswered_posts, "unanswered");

    @StringRes
    private final int textRes;
    private final String queryParamValue;

    DiscussionPostsFilter(@StringRes int textRes, String queryParamValue) {
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
    public String getQueryParamValue() {
        return queryParamValue;
    }
}
