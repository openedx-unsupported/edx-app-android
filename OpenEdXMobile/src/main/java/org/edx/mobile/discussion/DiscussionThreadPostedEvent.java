package org.edx.mobile.discussion;

import androidx.annotation.NonNull;

import org.edx.mobile.model.discussion.DiscussionThread;

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
