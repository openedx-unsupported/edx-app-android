package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.model.Page;

public abstract class GetCommentsListTask extends Task<Page<DiscussionComment>> {

    private static final int PAGE_SIZE = 20;

    @NonNull
    String responseId;
    int page = 1;

    public GetCommentsListTask(@NonNull Context context, @NonNull String responseId, int page) {
        super(context);
        this.responseId = responseId;
        this.page = page;
    }

    public Page<DiscussionComment> call() throws Exception {
        return environment.getDiscussionAPI().getCommentsList(responseId, PAGE_SIZE, page,
                DiscussionRequestFields.getRequestedFieldsList(environment.getConfig()));
    }
}
