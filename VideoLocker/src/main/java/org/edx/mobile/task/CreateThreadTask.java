package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.ThreadBody;

public abstract class CreateThreadTask extends Task<DiscussionThread> {
    @NonNull
    private final ThreadBody thread;

    public CreateThreadTask(@NonNull Context context, @NonNull ThreadBody thread) {
        super(context, Type.USER_INITIATED);
        this.thread = thread;
    }

    public DiscussionThread call( ) throws Exception{
        return environment.getDiscussionAPI().createThread(thread);
    }
}
