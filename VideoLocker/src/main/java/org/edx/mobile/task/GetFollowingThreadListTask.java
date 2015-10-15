package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.TopicThreads;

import org.edx.mobile.view.adapters.IPagination;

public abstract class GetFollowingThreadListTask extends
Task<TopicThreads> {

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



    public TopicThreads call( ) throws Exception{
        try {
            if(courseId!=null){

                String view;
                if (filter == DiscussionPostsFilter.UNREAD) view = "unread";
                else if (filter == DiscussionPostsFilter.UNANSWERED) view = "unanswered";
                else view = "";

                String order;
                if (orderBy == DiscussionPostsSort.LAST_ACTIVITY_AT) order = "last_activity_at";
                else if (orderBy == DiscussionPostsSort.VOTE_COUNT) order = "vote_count";
                else order = "";

                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;
                return environment.getDiscussionAPI().getFollowingThreadList(courseId, view, order, pageSize, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
