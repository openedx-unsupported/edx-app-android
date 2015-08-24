package org.edx.mobile.task;

import android.content.Context;

import com.qualcomm.qlearn.sdk.discussion.CourseTopics;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.view.adapters.IPagination;

public abstract class SearchThreadListTask extends
Task<TopicThreads> {

    String courseId;
    String text;
    IPagination pagination;

    public SearchThreadListTask(Context context, String courseId, String text, IPagination pagination) {
        super(context);
        this.courseId = courseId;
        this.text = text;
        this.pagination = pagination;
    }



    public TopicThreads call( ) throws Exception{
        try {

            if(courseId!=null){
                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;

                return environment.getDiscussionAPI().searchThreadList(courseId, text, pageSize, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
