package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.ThreadComments;

public abstract class GetCommentListTask extends Task<ThreadComments> {

    private static final int PAGE_SIZE = 20;

    String threadId;
    int page = 1;

    public GetCommentListTask(Context context, String threadId, int page) {
        super(context);
        this.threadId = threadId;
        this.page = page;
    }

    public ThreadComments call() throws Exception {
        try {

            if (threadId != null) {
                return environment.getDiscussionAPI().getCommentList(threadId, PAGE_SIZE, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
