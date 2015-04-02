package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

public abstract class GetHandoutTask extends Task<HandoutModel> {

    public GetHandoutTask(Context context) {
        super(context);
    }

    @Override
    protected HandoutModel doInBackground(Object... params) {
        try {
            EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) params[0];
            if(enrollment!=null){
                IApi api = ApiFactory.getCacheApiInstance(context);

                try {
                    // return instant data from cache
                    final HandoutModel model = api.getHandout
                            (enrollment.getCourse().getCourse_handouts(), true);
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

                return api.getHandout(enrollment.getCourse()
                        .getCourse_handouts(), false);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }

}
