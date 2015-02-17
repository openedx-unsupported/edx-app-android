package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public abstract class GetCourseInfoTask extends Task<CourseInfoModel> {

    public GetCourseInfoTask(Context context) {
        super(context);
    }

    @Override
    protected CourseInfoModel doInBackground(Object... params) {
        try {
            EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) params[0];
            if(enrollment!=null){
                Api api = new Api(context);
                try {
                    // return instant data from cache
                    final CourseInfoModel model = api.getCourseInfo(enrollment.getCourse().getCourse_about(), true);
                    if (model != null) {
                        handler.post(new Runnable() {
                            public void run() {
                                onFinish(model);
                                stopProgress();
                            }
                        });
                    }
                } catch(Exception ex) {
                    logger.error(ex);
                }

                return api.getCourseInfo(enrollment.getCourse().getCourse_about(), false);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }

}
