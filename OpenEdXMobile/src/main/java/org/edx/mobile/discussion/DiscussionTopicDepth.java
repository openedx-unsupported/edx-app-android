package org.edx.mobile.discussion;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorates {@link DiscussionTopic} with a "depth" and "postable" flag to facilitate usage in adapter view
 */
public class DiscussionTopicDepth {

    @NonNull
    private final DiscussionTopic discussionTopic;
    private final int depth;
    private final boolean postable; // TODO: Let the API decide which topics can be posted to

    public DiscussionTopicDepth(@NonNull DiscussionTopic topic, int depth, boolean postable) {
        this.discussionTopic = topic;
        this.depth = depth;
        this.postable = postable;
    }

    @NonNull
    public DiscussionTopic getDiscussionTopic() {
        return discussionTopic;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isPostable() {
        return postable;
    }

    @NonNull
    public static List<DiscussionTopicDepth> createFromDiscussionTopics(@NonNull List<DiscussionTopic> discussionTopics) {
        return createFromDiscussionTopics(discussionTopics, 0);
    }

    @NonNull
    private static List<DiscussionTopicDepth> createFromDiscussionTopics(@NonNull List<DiscussionTopic> discussionTopics, int depth) {
        List<DiscussionTopicDepth> discussionTopicDepths = new ArrayList<>();

        for (DiscussionTopic discussionTopic : discussionTopics) {
            final List<DiscussionTopic> children = discussionTopic.getChildren();
            if (null == children || children.isEmpty()) {
                discussionTopicDepths.add(new DiscussionTopicDepth(discussionTopic, depth, true));
            } else {
                discussionTopicDepths.add(new DiscussionTopicDepth(discussionTopic, depth, false));
                discussionTopicDepths.addAll(createFromDiscussionTopics(children, depth + 1));
            }
        }

        return discussionTopicDepths;
    }
}
