package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.services.ServiceManager;

import java.util.List;

public abstract class GetAnnouncementTask extends
        Task<List<AnnouncementsModel>> {
    EnrolledCoursesResponse enrollment;
    public GetAnnouncementTask(Context context, EnrolledCoursesResponse enrollment) {
        super(context);
        this.enrollment = enrollment;
    }

    public List<AnnouncementsModel> call() throws Exception{
        try {
            ServiceManager api = environment.getServiceManager();
            
            try {
                // return instant data from cache
                final List<AnnouncementsModel> list = api
                        .getAnnouncement(enrollment.getCourse().getCourse_updates(), true);
                if (list != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                onSuccess(list);
                            }catch (Exception ex){
                                logger.error(ex);
                            }
                            stopProgress();
                        }
                    });
                }
            } catch(Exception ex) {
                logger.error(ex);
            }
            
            return api.getAnnouncement(enrollment.getCourse().getCourse_updates(), false);
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }

}
