package org.edx.mobile.tta.task.content.course.scorm;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.scorm.ScormApi;
import org.edx.mobile.tta.scorm.ScormStartResponse;

public class StartScormTask extends Task<ScormStartResponse> {

    private String courseId;
    private String blockId;

    @Inject
    private ScormApi scormApi;

    public StartScormTask(Context context, String courseId, String blockId) {
        super(context);
        this.courseId = courseId;
        this.blockId = blockId;
    }

    @Override
    public ScormStartResponse call() throws Exception {
        return scormApi.scormStart(courseId, blockId).execute().body();
    }
}
