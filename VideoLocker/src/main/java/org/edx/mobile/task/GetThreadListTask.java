package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.TopicThreads;

import org.edx.mobile.view.adapters.IPagination;

import java.util.List;

public abstract class GetThreadListTask extends
Task<TopicThreads> {

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



    public TopicThreads call( ) throws Exception{
        try {

            if(courseId!=null){

                String view;
                if (filter == DiscussionPostsFilter.Unread) view = "unread";
                else if (filter == DiscussionPostsFilter.Unanswered) view = "unanswered";
                else view = "";

                String order;
                if (orderBy == DiscussionPostsSort.LastActivityAt) order = "last_activity_at";
                else if (orderBy == DiscussionPostsSort.VoteCount) order = "vote_count";
                else order = "";

                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;

                return environment.getDiscussionAPI().getThreadList(courseId, topicIds,
                        view,
                        order,
                        pageSize,
                        page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
