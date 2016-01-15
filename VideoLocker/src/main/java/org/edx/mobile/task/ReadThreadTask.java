package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.http.RetroHttpException;

import de.greenrobot.event.EventBus;

public class ReadThreadTask extends Task<DiscussionThread> {
    private DiscussionThread thread;
    private boolean read;

    public ReadThreadTask(@NonNull Context context,
                          @NonNull DiscussionThread thread, boolean read) {
        super(context);
        this.thread = thread;
        this.read = read;
    }

    @Override
    public DiscussionThread call() throws RetroHttpException {
        return environment.getDiscussionAPI().readThread(thread, read);
    }

    @Override
    protected void onSuccess(DiscussionThread discussionThread) {
        EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        handle(e);
    }
}
