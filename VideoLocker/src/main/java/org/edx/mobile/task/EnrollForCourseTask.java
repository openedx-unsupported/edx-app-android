package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

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
                IApi api = ApiFactory.getCacheApiInstance(context);
                try {
                    return api.doEnrollInACourse(courseId,emailOptIn);
                } catch(Exception ex) {
                    logger.error(ex, true);
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
