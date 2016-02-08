package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.http.RetroHttpException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GetThreadListTask extends Task<TopicThreads> {
    static final int PAGE_SIZE = 20;

    final String courseId;
    final DiscussionTopic topic;
    final DiscussionPostsFilter filter;
    final DiscussionPostsSort orderBy;
    final int page;

    public GetThreadListTask(Context context, String courseId,
                             DiscussionTopic topic,
                             DiscussionPostsFilter filter,
                             DiscussionPostsSort orderBy,
                             int page) {
        super(context);
        this.courseId = courseId;
        this.topic = topic;
        this.filter = filter;
        this.orderBy = orderBy;
        this.page = page;
    }

    public TopicThreads call() throws Exception {
        try {
            if (courseId != null) {
                if (!topic.isFollowingType()) {
                    return environment.getDiscussionAPI().getThreadList(courseId,
                            getAllTopicIds(), filter.getQueryParamValue(),
                            orderBy.getQueryParamValue(), PAGE_SIZE, page);
                } else {
                    return environment.getDiscussionAPI().getFollowingThreadList(courseId,
                            filter.getQueryParamValue(), orderBy.getQueryParamValue(),
                            PAGE_SIZE, page);
                }
            }
        } catch (RetroHttpException ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }

    @NonNull
    public List<String> getAllTopicIds() {
        if (topic.isAllType()) {
            return Collections.EMPTY_LIST;
        } else {
            final List<String> ids = new ArrayList<>();
            appendTopicIds(topic, ids);
            return ids;
        }
    }

    private void appendTopicIds(@NonNull DiscussionTopic dTopic, @NonNull List<String> ids) {
        String id = dTopic.getIdentifier();
        if (!TextUtils.isEmpty(id)) {
            ids.add(id);
        }
        for (DiscussionTopic child : dTopic.getChildren()) {
            appendTopicIds(child, ids);
        }
    }
}
