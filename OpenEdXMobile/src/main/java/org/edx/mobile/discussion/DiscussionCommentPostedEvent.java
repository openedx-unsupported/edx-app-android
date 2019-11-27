package org.edx.mobile.discussion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DiscussionCommentPostedEvent {
    @NonNull
    private final DiscussionComment comment;

    @Nullable
    private final DiscussionComment parent;

    public DiscussionCommentPostedEvent(@NonNull DiscussionComment comment, @Nullable DiscussionComment parent) {
        this.comment = comment;
        this.parent = parent;
    }

    /**
     * Provides the response or the comment that was posted.
     *
     * @return The response or comment.
     */
    @NonNull
    public DiscussionComment getComment() {
        return comment;
    }

    /**
     * Provides the parent of the comment that was posted.
     * Note: The parent is always null in case of a response being posted.
     *
     * @return The parent of the comment if available i.e. response.
     */
    @Nullable
    public DiscussionComment getParent() {
        return parent;
    }
}
