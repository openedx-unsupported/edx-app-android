package org.edx.mobile.task;

import android.content.Context;

import com.qualcomm.qlearn.sdk.discussion.DiscussionPostsFilter;
import com.qualcomm.qlearn.sdk.discussion.DiscussionPostsSort;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.view.CourseDiscussionPostsThreadFragment;
import org.edx.mobile.view.adapters.IPagination;

import java.util.Date;

public abstract class GetThreadListTask extends
Task<TopicThreads> {

    String courseId;
    String topicId;
    DiscussionPostsFilter filter;
    DiscussionPostsSort orderBy;
    IPagination pagination;

    public GetThreadListTask(Context context,
                             String courseId,
                             String topicId,
                             DiscussionPostsFilter filter,
                             DiscussionPostsSort orderBy,
                             IPagination pagination) {
        super(context);
        this.courseId = courseId;
        this.topicId = topicId;
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

                return environment.getDiscussionAPI().getThreadList(courseId, topicId,
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
