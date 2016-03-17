package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.CourseTopics;

public abstract class GetTopicListTask extends
Task<CourseTopics> {

    @NonNull
    String courseId;

    public GetTopicListTask(@NonNull Context context, @NonNull String courseId) {
        super(context);
        this.courseId = courseId;
    }



    public CourseTopics call( ) throws Exception{
        return environment.getDiscussionAPI().getTopicList(courseId);
    }
}
