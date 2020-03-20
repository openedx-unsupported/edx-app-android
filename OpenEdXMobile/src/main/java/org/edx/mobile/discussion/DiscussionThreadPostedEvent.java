package org.edx.mobile.discussion;

import androidx.annotation.NonNull;

public class DiscussionThreadPostedEvent {
    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadPostedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
