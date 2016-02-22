package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.model.Page;

public abstract class GetResponsesListTask extends Task<Page<DiscussionComment>> {

    String threadId;
    int page = 1;
    boolean isQuestionType;
    boolean shouldGetEndorsed;

    public GetResponsesListTask(Context context, String threadId, int page, boolean isQuestionType,
                                boolean shouldGetEndorsed) {
        super(context);
        this.threadId = threadId;
        this.page = page;
        this.isQuestionType = isQuestionType;
        this.shouldGetEndorsed = shouldGetEndorsed;
    }

    public Page<DiscussionComment> call() throws Exception {
        try {
            if (threadId != null) {
                // Question threads require the 'endorsed' parameter.
                if (isQuestionType) {
                    if (shouldGetEndorsed) {
                        // Get all the endorsed responses in one go without pagination,
                        // as we don't expect to have a large number of them.
                        return environment.getDiscussionAPI().getResponsesListForQuestion(threadId,
                                true);
                    }
                    return environment.getDiscussionAPI().getResponsesListForQuestion(threadId,
                            page, shouldGetEndorsed);
                }
                return environment.getDiscussionAPI().getResponsesList(threadId, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
