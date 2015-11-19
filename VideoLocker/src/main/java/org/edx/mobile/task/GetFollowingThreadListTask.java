package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.view.adapters.IPagination;

public abstract class GetFollowingThreadListTask extends Task<TopicThreads> {
    String courseId;
    DiscussionPostsSort orderBy;
    DiscussionPostsFilter filter;
    IPagination pagination;

    public GetFollowingThreadListTask(Context context, String courseId,
                                      DiscussionPostsFilter filter,
                                      DiscussionPostsSort orderBy,
                                      IPagination pagination) {
        super(context);
        this.courseId = courseId;
        this.orderBy = orderBy;
        this.filter = filter;
        this.pagination = pagination;
    }


    public TopicThreads call() throws Exception {
        try {
            if (courseId != null) {
                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;
                return environment.getDiscussionAPI().getFollowingThreadList(courseId,
                        filter.getQueryParamValue(), orderBy.getQueryParamValue(), pageSize, page);
            }
        } catch (RetroHttpException ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
