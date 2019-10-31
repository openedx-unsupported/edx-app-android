package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.course.CourseAPI;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.course.CourseStructureV1Model;
import org.humana.mobile.task.Task;

import static org.humana.mobile.course.CourseAPI.normalizeCourseStructure;

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
