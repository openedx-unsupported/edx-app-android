package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.http.RetroHttpException;

public abstract class GetThreadTask extends Task<DiscussionThread> {

    @NonNull
    final String threadId;

    public GetThreadTask(@NonNull Context context, @NonNull String threadId) {
        super(context);
        this.threadId = threadId;
    }

    public DiscussionThread call() throws Exception {
        return environment.getDiscussionAPI().getThread(threadId);
    }
}
