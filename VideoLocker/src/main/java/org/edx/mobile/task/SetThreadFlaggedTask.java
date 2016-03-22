package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;

public abstract class SetThreadFlaggedTask extends Task<DiscussionThread> {
    private final DiscussionThread thread;
    private final boolean flagged;

    public SetThreadFlaggedTask(@NonNull Context context,
                                @NonNull DiscussionThread thread, boolean flagged) {
        super(context);
        this.thread = thread;
        this.flagged = flagged;
    }

    public DiscussionThread call() throws Exception {
        return environment.getDiscussionAPI().setThreadFlagged(thread, flagged);
    }
}
