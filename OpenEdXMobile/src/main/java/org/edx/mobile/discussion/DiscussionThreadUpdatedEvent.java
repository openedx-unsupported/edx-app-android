package org.edx.mobile.discussion;

import androidx.annotation.NonNull;

import org.edx.mobile.discussion.DiscussionThread;

public class DiscussionThreadUpdatedEvent {

    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadUpdatedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
