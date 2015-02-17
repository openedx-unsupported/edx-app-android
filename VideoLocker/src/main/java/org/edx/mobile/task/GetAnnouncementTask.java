package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import java.util.List;

public abstract class GetAnnouncementTask extends
        Task<List<AnnouncementsModel>> {

    public GetAnnouncementTask(Context context) {
        super(context);

    }

    protected List<AnnouncementsModel> doInBackground(Object... params) {
        try {
            EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) params[0];
            Api api = new Api(context);
            
            try {
                // return instant data from cache
                final List<AnnouncementsModel> list = api
                        .getAnnouncement(enrollment.getCourse().getCourse_updates(), true);
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
            
            return api.getAnnouncement(enrollment.getCourse().getCourse_updates(), false);
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }

}
