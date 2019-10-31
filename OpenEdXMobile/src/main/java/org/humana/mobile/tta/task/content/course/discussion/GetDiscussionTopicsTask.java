package org.humana.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.discussion.CourseTopics;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.remote.api.DiscussionApi;

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
