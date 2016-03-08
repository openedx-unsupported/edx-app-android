package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.http.RetroHttpException;

public abstract class GetThreadTask extends Task<DiscussionThread> {

    final String threadId;

    public GetThreadTask(Context context, String threadId) {
        super(context);
        this.threadId = threadId;
    }

    public DiscussionThread call() throws Exception {
        try {
            if (threadId != null) {
                return environment.getDiscussionAPI().getThread(threadId);
            }
        } catch (RetroHttpException ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
