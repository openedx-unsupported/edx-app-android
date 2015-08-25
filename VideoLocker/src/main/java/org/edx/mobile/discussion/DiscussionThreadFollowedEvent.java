package org.edx.mobile.discussion;

import android.support.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;

public class DiscussionThreadFollowedEvent {

    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadFollowedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
