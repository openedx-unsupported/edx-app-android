package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.view.adapters.IPagination;

import java.util.List;

public abstract class GetThreadListTask extends Task<TopicThreads> {
    String courseId;
    List<String> topicIds;
    DiscussionPostsFilter filter;
    DiscussionPostsSort orderBy;
    IPagination pagination;

    public GetThreadListTask(Context context,
                             String courseId,
                             List<String> topicIds,
                             DiscussionPostsFilter filter,
                             DiscussionPostsSort orderBy,
                             IPagination pagination) {
        super(context);
        this.courseId = courseId;
        this.topicIds = topicIds;
        this.filter = filter;
        this.orderBy = orderBy;
        this.pagination = pagination;
    }

    public TopicThreads call() throws Exception {
        try {
            if (courseId != null) {
                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;
                return environment.getDiscussionAPI().getThreadList(courseId, topicIds,
                        filter.getQueryParamValue(), orderBy.getQueryParamValue(), pageSize, page);
            }
        } catch (RetroHttpException ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
