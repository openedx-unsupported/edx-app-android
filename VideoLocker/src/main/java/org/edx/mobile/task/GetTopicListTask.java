package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.CourseTopics;

public abstract class GetTopicListTask extends
Task<CourseTopics> {

    String courseId;

    public GetTopicListTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }



    public CourseTopics call( ) throws Exception{
        try {

            if(courseId!=null){

                return environment.getDiscussionAPI().getTopicList(courseId);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
