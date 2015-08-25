package org.edx.mobile.discussion;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DiscussionCommentPostedEvent {
    @NonNull
    private final DiscussionComment comment;

    @Nullable
    private final DiscussionComment parent;

    public DiscussionCommentPostedEvent(@NonNull DiscussionComment comment, DiscussionComment parent) {
        this.comment = comment;
        this.parent = parent;
    }

    @NonNull
    public DiscussionComment getComment() {
        return comment;
    }

    @Nullable
    public DiscussionComment getParent() {
        return parent;
    }
}
