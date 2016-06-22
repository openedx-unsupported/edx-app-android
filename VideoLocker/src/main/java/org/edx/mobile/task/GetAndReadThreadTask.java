package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.http.HttpException;

import de.greenrobot.event.EventBus;

public class GetAndReadThreadTask extends Task<DiscussionThread> {
    private final DiscussionThread thread;

    public GetAndReadThreadTask(@NonNull Context context,
                                @NonNull DiscussionThread thread) {
        super(context);
        this.thread = thread;
    }

    @Override
    public DiscussionThread call() throws HttpException {
        return environment.getDiscussionAPI().setThreadRead(thread, true);
    }

    @Override
    protected void onSuccess(DiscussionThread discussionThread) {
        discussionThread = thread.patchObject(discussionThread);
        EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
    }
}
