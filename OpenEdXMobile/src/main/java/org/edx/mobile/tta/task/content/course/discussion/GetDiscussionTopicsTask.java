package org.edx.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.DiscussionApi;

public class GetDiscussionTopicsTask extends Task<CourseTopics> {

    private String courseId;

    @Inject
    private DiscussionApi api;

    public GetDiscussionTopicsTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public CourseTopics call() throws Exception {
        return api.getCourseTopics(courseId).execute().body();
    }
}
