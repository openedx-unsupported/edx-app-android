package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionComment;

public abstract class SetCommentVotedTask extends Task<DiscussionComment> {
    @NonNull
    private final DiscussionComment comment;
    @NonNull
    private final boolean voted;

    public SetCommentVotedTask(@NonNull Context context,
                               @NonNull DiscussionComment comment, boolean voted) {
        super(context, Type.USER_INITIATED);
        this.comment = comment;
        this.voted = voted;
    }

    public DiscussionComment call() throws Exception {
        return environment.getDiscussionAPI().setCommentVoted(comment, voted);
    }

    @Override
    protected void onSuccess(DiscussionComment discussionComment) {
        discussionComment = comment.patchObject(discussionComment);
    }
}
