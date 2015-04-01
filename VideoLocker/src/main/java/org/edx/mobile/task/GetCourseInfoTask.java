package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

public abstract class GetCourseInfoTask extends Task<CourseInfoModel> {

    public GetCourseInfoTask(Context context) {
        super(context);
    }

    @Override
    protected CourseInfoModel doInBackground(Object... params) {
        try {
            EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) params[0];
            if(enrollment!=null){
                IApi api = ApiFactory.getCacheApiInstance(context);
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
            logger.error(ex, true);
        }
        return null;
    }

}
