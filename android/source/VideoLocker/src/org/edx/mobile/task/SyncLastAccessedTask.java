package org.edx.mobile.task;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;

import android.content.Context;

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
        }
        return null;
    }
}
