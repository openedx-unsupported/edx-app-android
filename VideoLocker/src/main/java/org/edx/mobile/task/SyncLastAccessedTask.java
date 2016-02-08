package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.LastAccessedSubsectionResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.user.UserAPI;

public abstract class SyncLastAccessedTask extends Task<LastAccessedSubsectionResponse> {

    String courseId;
    String lastVisitedModuleId;
    UserAPI userAPI;

    public SyncLastAccessedTask(Context context, String courseId, String lastVisitedModuleId, UserAPI userAPI) {
        super(context);
        this.courseId = courseId;
        this.lastVisitedModuleId = lastVisitedModuleId;
        this.userAPI = userAPI;
    }

    @Override
    public LastAccessedSubsectionResponse call() throws Exception {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        String username = pref.getCurrentUserProfile().username;

        try {

            if (courseId != null && lastVisitedModuleId != null) {
                LastAccessedSubsectionResponse res = userAPI.syncLastAccessedSubsection(
                        username,
                        courseId,
                        lastVisitedModuleId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
