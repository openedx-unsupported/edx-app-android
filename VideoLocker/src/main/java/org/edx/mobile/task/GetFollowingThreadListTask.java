package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.http.RetroHttpException;

public abstract class GetFollowingThreadListTask extends Task<TopicThreads> {
    private static final int PAGE_SIZE = 20;
    final String courseId;
    final DiscussionPostsSort orderBy;
    final DiscussionPostsFilter filter;
    final int page;

    public GetFollowingThreadListTask(Context context, String courseId,
                                      DiscussionPostsFilter filter,
                                      DiscussionPostsSort orderBy,
                                      int page) {
        super(context);
        this.courseId = courseId;
        this.orderBy = orderBy;
        this.filter = filter;
        this.page = page;
    }


    public TopicThreads call() throws Exception {
        try {
            if (courseId != null) {
                return environment.getDiscussionAPI().getFollowingThreadList(courseId,
                        filter.getQueryParamValue(), orderBy.getQueryParamValue(), PAGE_SIZE, page);
            }
        } catch (RetroHttpException ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
