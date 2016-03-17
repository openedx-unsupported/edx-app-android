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
        ServiceManager api = environment.getServiceManager();

        // return instant data from cache if available
        List<AnnouncementsModel> list = api
                .getAnnouncement(enrollment.getCourse().getCourse_updates(), true);
        if (list == null) {
            list = api.getAnnouncement(enrollment.getCourse().getCourse_updates(), false);
        }

        return list;
    }

}
