package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GetThreadListTask extends Task<Page<DiscussionThread>> {

    @NonNull
    final String courseId;
    @NonNull
    final DiscussionTopic topic;
    @NonNull
    final DiscussionPostsFilter filter;
    @NonNull
    final DiscussionPostsSort orderBy;
    final int page;

    public GetThreadListTask(@NonNull Context context, @NonNull String courseId,
                             @NonNull DiscussionTopic topic,
                             @NonNull DiscussionPostsFilter filter,
                             @NonNull DiscussionPostsSort orderBy,
                             int page) {
        super(context);
        this.courseId = courseId;
        this.topic = topic;
        this.filter = filter;
        this.orderBy = orderBy;
        this.page = page;
    }

    public Page<DiscussionThread> call() throws Exception {
        final List<String> requestedFields = DiscussionRequestFields.getRequestedFieldsList(
                environment.getConfig());
        if (!topic.isFollowingType()) {
            return environment.getDiscussionAPI().getThreadList(courseId,
                    getAllTopicIds(), filter.getQueryParamValue(),
                    orderBy.getQueryParamValue(), page, requestedFields);
        } else {
            return environment.getDiscussionAPI().getFollowingThreadList(courseId,
                    filter.getQueryParamValue(), orderBy.getQueryParamValue(),
                    page, requestedFields);
        }
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
