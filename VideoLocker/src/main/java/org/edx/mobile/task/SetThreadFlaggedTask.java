package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;

import de.greenrobot.event.EventBus;

public abstract class SetThreadFlaggedTask extends Task<DiscussionThread> {
    @NonNull
    private final DiscussionThread thread;
    @NonNull
    private final boolean flagged;

    public SetThreadFlaggedTask(@NonNull Context context,
                                @NonNull DiscussionThread thread, boolean flagged) {
        super(context, Type.USER_INITIATED);
        this.thread = thread;
        this.flagged = flagged;
    }

    public DiscussionThread call() throws Exception {
        return environment.getDiscussionAPI().setThreadFlagged(thread, flagged);
    }

    @Override
    protected void onSuccess(DiscussionThread discussionThread) {
        discussionThread = thread.patchObject(discussionThread);
        EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
    }
}
