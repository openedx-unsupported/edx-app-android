package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.exception.AuthException;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

import java.util.List;

public abstract class GetEnrolledCoursesTask extends Task<List<EnrolledCoursesResponse>> {

    public GetEnrolledCoursesTask(Context context) {
        super(context);
    }

    @Override
    protected List<EnrolledCoursesResponse> doInBackground(Object... params) {
        try { 
            IApi api = ApiFactory.getCacheApiInstance(context);

            // TODO Handle below response, navigate to login?
            /* 
             * 07-08 10:40:23.278: D/Api(1119): getEnrolledCourses={"detail": "Authentication credentials were not provided."}
             */
            
            // return instant cached data
            try {
                final List<EnrolledCoursesResponse> list = api
                        .getEnrolledCourses(true);
                if (list != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            onFinish(list);
                            stopProgress();
                        }
                    });
                }
            } catch(Exception ex) {
                logger.error(ex);
            }
            
            return api.getEnrolledCourses();
        } catch(AuthException ex) {
            handle(ex);
            logger.error(ex);
        } catch(Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
