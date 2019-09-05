package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.task.Task;

import static org.edx.mobile.course.CourseAPI.normalizeCourseStructure;

public class GetBlockComponentFromServerTask extends Task<CourseComponent> {

    private String blockId, courseId;

    @Inject
    private CourseAPI api;

    public GetBlockComponentFromServerTask(Context context, String blockId, String courseId) {
        super(context);
        this.blockId = blockId;
        this.courseId = courseId;
    }

    @Override
    public CourseComponent call() throws Exception {
        CourseStructureV1Model model = api.getBlockComponentWithoutStale(blockId, courseId).execute().body();
        return (CourseComponent) normalizeCourseStructure(model, courseId);
    }
}
