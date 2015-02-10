package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import java.util.ArrayList;

public abstract class EnrollForCourseTask extends Task<Boolean> {

    public EnrollForCourseTask(Context context) {
        super(context);
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            String courseId = (String) (params[0]);
            boolean emailOptIn = (boolean) (params[1]);
            if(courseId!=null){
                Api api = new Api(context);
                try {
                    final boolean isCourseEnrolled = api.enrollInACourse(courseId,emailOptIn);
                    return  isCourseEnrolled;
                } catch(Exception ex) {
                    logger.error(ex);
                }
            }
            return false;
        } catch(Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return false;
    }
}
