package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.discussion.CourseTopics;

import java.util.List;

public abstract class GetSpecificCourseTopicsTask extends Task<CourseTopics> {
    @NonNull
    private final String courseId;

    @NonNull
    private final List<String> topicIds;

    public GetSpecificCourseTopicsTask(@NonNull Context context, @NonNull String courseId,
                                       @NonNull List<String> topicIds) {
        super(context);
        this.courseId = courseId;
        this.topicIds = topicIds;
    }

    public CourseTopics call() throws Exception {
        return environment.getDiscussionAPI().getSpecificCourseTopics(courseId, topicIds);
    }
}
