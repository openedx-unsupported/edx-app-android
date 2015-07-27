package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.services.ServiceManager;

public abstract class GetHandoutTask extends Task<HandoutModel> {
    EnrolledCoursesResponse enrollment;
    public GetHandoutTask(Context context,EnrolledCoursesResponse enrollment ) {
        super(context);
        this.enrollment = enrollment;
    }

    @Override
    public HandoutModel call() throws Exception{
        try {

            if(enrollment!=null){
                ServiceManager api = environment.getServiceManager();

                try {
                    // return instant data from cache
                    final HandoutModel model = api.getHandout
                            (enrollment.getCourse().getCourse_handouts(), true);
                    if (model != null) {
                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    onSuccess(model);
                                } catch (Exception e) {
                                    logger.error(e);
                                }
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
