package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionComment;

public abstract class SetCommentFlaggedTask extends Task<DiscussionComment> {
    private final DiscussionComment comment;
    private final boolean flagged;

    public SetCommentFlaggedTask(@NonNull Context context,
                                 @NonNull DiscussionComment comment, boolean flagged) {
        super(context);
        this.comment = comment;
        this.flagged = flagged;
    }

    public DiscussionComment call() throws Exception {
        return environment.getDiscussionAPI().setCommentFlagged(comment, flagged);
    }
}
