package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

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
                IApi api = ApiFactory.getCacheApiInstance(context);
                SyncLastAccessedSubsectionResponse res = api.doSyncLastAccessedSubsection(
                        courseId, lastVisitedModuleId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
