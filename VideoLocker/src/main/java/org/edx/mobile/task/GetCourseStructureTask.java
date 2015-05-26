package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.course.CourseStructureJsonHandler;
import org.edx.mobile.model.course.CourseStructureV1Model;

public abstract class GetCourseStructureTask extends
Task<CourseStructureV1Model> {

    public GetCourseStructureTask(Context context) {
        super(context);
    }

    protected CourseStructureV1Model doInBackground(Object... params) {
        try {
            String courseId = (String) (params[0]);
            if(courseId!=null){
                Api api = new Api(context);
                String result = api.getCourseStructure(courseId, false);
               // result = result.replaceAll("\"data\"", "\"data_block\"");
                final CourseStructureV1Model model = new CourseStructureJsonHandler().processInput(result);
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
