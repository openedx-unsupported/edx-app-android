package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.model.Page;

public abstract class GetResponsesListTask extends Task<Page<DiscussionComment>> {

    @NonNull
    String threadId;
    int page = 1;
    boolean isQuestionType;
    boolean shouldGetEndorsed;

    public GetResponsesListTask(@NonNull Context context, @NonNull String threadId, int page,
                                boolean isQuestionType, boolean shouldGetEndorsed) {
        super(context);
        this.threadId = threadId;
        this.page = page;
        this.isQuestionType = isQuestionType;
        this.shouldGetEndorsed = shouldGetEndorsed;
    }

    public Page<DiscussionComment> call() throws Exception {
        if (isQuestionType) {
            return environment.getDiscussionAPI().getResponsesListForQuestion(threadId,
                    page, shouldGetEndorsed);
        }
        return environment.getDiscussionAPI().getResponsesList(threadId, page);
    }
}
