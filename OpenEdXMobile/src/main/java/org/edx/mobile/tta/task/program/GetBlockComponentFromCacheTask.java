package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.Task;

public class GetBlockComponentFromCacheTask extends Task<CourseComponent> {

    private String blockId, courseId;

    @Inject
    private CourseAPI api;

    public GetBlockComponentFromCacheTask(Context context, String blockId, String courseId) {
        super(context);
        this.blockId = blockId;
        this.courseId = courseId;
    }

    @Override
    public CourseComponent call() throws Exception {
        return api.getBlockComponentFromCache(blockId, courseId);
    }
}
