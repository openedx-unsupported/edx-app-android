package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;

public abstract class SetCommentFlaggedTask extends Task<DiscussionComment> {
    @NonNull
    private final DiscussionComment comment;
    @NonNull
    private final boolean flagged;

    public SetCommentFlaggedTask(@NonNull Context context,
                                 @NonNull DiscussionComment comment, boolean flagged) {
        super(context, Type.USER_INITIATED);
        this.comment = comment;
        this.flagged = flagged;
    }

    public DiscussionComment call() throws Exception {
        return environment.getDiscussionAPI().setCommentFlagged(comment, flagged);
    }

    @Override
    protected void onSuccess(DiscussionComment discussionComment) {
        discussionComment = comment.patchObject(discussionComment);
    }
}
