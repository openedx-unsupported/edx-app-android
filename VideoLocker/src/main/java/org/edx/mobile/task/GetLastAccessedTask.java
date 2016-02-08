package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.LastAccessedSubsectionResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.user.UserAPI;

public abstract class GetLastAccessedTask extends Task<LastAccessedSubsectionResponse> {

    String courseId;
    UserAPI userAPI;

    public GetLastAccessedTask(Context context,  String courseId, UserAPI userAPI) {
        super(context);
        this.courseId = courseId;
        this.userAPI = userAPI;
    }

    @Override
    public LastAccessedSubsectionResponse call() throws Exception{
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        String username = pref.getCurrentUserProfile().username;

        try {
            if(courseId!=null){
                LastAccessedSubsectionResponse res = userAPI.getLastAccessedSubsection(username, courseId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
