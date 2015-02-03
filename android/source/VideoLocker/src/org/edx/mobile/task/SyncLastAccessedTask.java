package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;

public abstract class SyncLastAccessedTask extends Task<SyncLastAccessedSubsectionResponse> {

    public SyncLastAccessedTask(Context context) {
        super(context);
    }

    @Override
    protected SyncLastAccessedSubsectionResponse doInBackground(Object... params) {
        try {
            String courseId = (String)params[0];
            String lastVisitedModuleId = (String)params[1];
            if(courseId!=null && lastVisitedModuleId !=null){
                Api api = new Api(context);
                SyncLastAccessedSubsectionResponse res = api.syncLastAccessedSubsection(
                        courseId, lastVisitedModuleId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }
}
