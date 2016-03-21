package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;

public abstract class SetThreadFollowedTask extends Task<DiscussionThread> {
    private final DiscussionThread thread;
    private final boolean followed;

    public SetThreadFollowedTask(@NonNull Context context,
                                 @NonNull DiscussionThread thread, boolean followed) {
        super(context);
        this.thread = thread;
        this.followed = followed;
    }

    public DiscussionThread call() throws Exception {
        return environment.getDiscussionAPI().setThreadFollowed(thread, followed);
    }
}
