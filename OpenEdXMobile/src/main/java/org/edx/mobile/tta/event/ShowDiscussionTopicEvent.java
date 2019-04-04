package org.edx.mobile.tta.event;

import org.edx.mobile.discussion.DiscussionTopicDepth;

public class ShowDiscussionTopicEvent {
    private DiscussionTopicDepth topicDepth;

    public ShowDiscussionTopicEvent(DiscussionTopicDepth topicDepth) {
        this.topicDepth = topicDepth;
    }

    public DiscussionTopicDepth getTopicDepth() {
        return topicDepth;
    }
}
