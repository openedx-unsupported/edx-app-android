package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;

import de.greenrobot.event.EventBus;

public abstract class SetThreadFollowedTask extends Task<DiscussionThread> {
    @NonNull
    private final DiscussionThread thread;
    @NonNull
    private final boolean followed;

    public SetThreadFollowedTask(@NonNull Context context,
                                 @NonNull DiscussionThread thread, boolean followed) {
        super(context, Type.USER_INITIATED);
        this.thread = thread;
        this.followed = followed;
    }

    public DiscussionThread call() throws Exception {
        return environment.getDiscussionAPI().setThreadFollowed(thread, followed);
    }

    @Override
    protected void onSuccess(DiscussionThread discussionThread) {
        discussionThread = thread.patchObject(discussionThread);
        EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
    }
}
