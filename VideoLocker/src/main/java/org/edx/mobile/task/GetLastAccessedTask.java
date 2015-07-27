package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.services.ServiceManager;

public abstract class GetLastAccessedTask extends Task<SyncLastAccessedSubsectionResponse> {

    String courseId;
    public GetLastAccessedTask(Context context,  String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public SyncLastAccessedSubsectionResponse call() throws Exception{
        try {

            if(courseId!=null){
                ServiceManager api = environment.getServiceManager();
                SyncLastAccessedSubsectionResponse res = api.getLastAccessedSubsection(courseId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
