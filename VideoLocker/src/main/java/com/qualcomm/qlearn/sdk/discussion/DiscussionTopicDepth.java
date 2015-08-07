package com.qualcomm.qlearn.sdk.discussion;

import java.util.ArrayList;
import java.util.List;

public class DiscussionTopicDepth {

    private DiscussionTopic discussionTopic;
    private int depth = 0;

    public DiscussionTopicDepth(DiscussionTopic topic, int depth) {
        this.discussionTopic = topic;
        this.depth = depth;
    }

    public DiscussionTopic getDiscussionTopic() {
        return discussionTopic;
    }

    public void setDiscussionTopic(DiscussionTopic discussionTopic) {
        this.discussionTopic = discussionTopic;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * This method decorates a discussion topic to add a depth parameter to indicate how deep in the tree the topic is
     * @param discussionTopics
     * @return List of DiscussionTopicDepth objects
     */
    public static List<DiscussionTopicDepth> createFromDiscussionTopics(List<DiscussionTopic> discussionTopics) {
        List<DiscussionTopicDepth> discussionTopicDepths = new ArrayList<>();

        // TODO: To handle nested topics beyond 1 level deep, turn this into a recursive method
        for (DiscussionTopic discussionTopic : discussionTopics) {
            discussionTopicDepths.add(new DiscussionTopicDepth(discussionTopic, 0));

            List<DiscussionTopic> children = discussionTopic.getChildren();
            if (children != null && !children.isEmpty()) {
                for (DiscussionTopic child : children) {
                    discussionTopicDepths.add(new DiscussionTopicDepth(child, 1));
                }
            }
        }

        return discussionTopicDepths;
    }

}
