package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.ThreadComments;

public abstract class GetResponsesListTask extends Task<ThreadComments> {

    private static final int PAGE_SIZE = 20;

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

    public ThreadComments call() throws Exception {
        try {
            if (threadId != null) {
                if (isQuestionType) {
                    return environment.getDiscussionAPI().getResponsesListForQuestion(threadId,
                            PAGE_SIZE, page, shouldGetEndorsed);
                }
                return environment.getDiscussionAPI().getResponsesList(threadId, PAGE_SIZE, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
