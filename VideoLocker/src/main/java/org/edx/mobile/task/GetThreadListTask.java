package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.http.RetroHttpException;

import java.util.List;

public abstract class GetThreadListTask extends Task<TopicThreads> {
    static final int PAGE_SIZE = 20;

    final String courseId;
    final List<String> topicIds;
    final DiscussionPostsFilter filter;
    final DiscussionPostsSort orderBy;
    final int page;

    public GetThreadListTask(Context context,
                             String courseId,
                             List<String> topicIds,
                             DiscussionPostsFilter filter,
                             DiscussionPostsSort orderBy,
                             int page) {
        super(context);
        this.courseId = courseId;
        this.topicIds = topicIds;
        this.filter = filter;
        this.orderBy = orderBy;
        this.page = page;
    }

    public TopicThreads call() throws Exception {
        try {
            if (courseId != null) {
                return environment.getDiscussionAPI().getThreadList(courseId, topicIds,
                        filter.getQueryParamValue(), orderBy.getQueryParamValue(), PAGE_SIZE, page);
            }
        } catch (RetroHttpException ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
