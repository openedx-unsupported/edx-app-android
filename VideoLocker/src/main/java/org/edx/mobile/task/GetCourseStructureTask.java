package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.ServiceManager;

public abstract class GetCourseStructureTask extends
Task<CourseComponent> {

    public GetCourseStructureTask(Context context) {
        super(context);
    }

    protected CourseComponent doInBackground(Object... params) {
        try {
            String courseId = (String) (params[0]);
            if(courseId!=null){
                final CourseComponent model = ServiceManager.getInstance().getCourseStructure(courseId, false);
                if (model != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            onFinish(model);
                            stopProgress();
                        }
                    });
                }
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
