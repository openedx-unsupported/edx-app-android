package org.edx.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.DiscussionApi;

public class CreateDiscussionCommentTask extends Task<DiscussionComment> {

    private String threadId;
    private String comment;
    private String parentCommentId;

    @Inject
    private DiscussionApi discussionApi;

    public CreateDiscussionCommentTask(Context context, String threadId, String comment, String parentCommentId) {
        super(context);
        this.threadId = threadId;
        this.comment = comment;
        this.parentCommentId = parentCommentId;
    }

    @Override
    public DiscussionComment call() throws Exception {
        return discussionApi.createComment(threadId, comment, parentCommentId).execute().body();
    }
}
