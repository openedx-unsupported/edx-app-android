package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.model.Page;

public abstract class GetCommentsListTask extends Task<Page<DiscussionComment>> {

    private static final int PAGE_SIZE = 20;

    String responseId;
    int page = 1;

    public GetCommentsListTask(Context context, String responseId, int page) {
        super(context);
        this.responseId = responseId;
        this.page = page;
    }

    public Page<DiscussionComment> call() throws Exception {
        try {
            if (responseId != null) {
                return environment.getDiscussionAPI().getCommentsList(responseId, PAGE_SIZE, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
