package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

public abstract class GetLastAccessedTask extends Task<SyncLastAccessedSubsectionResponse> {

    public GetLastAccessedTask(Context context) {
        super(context);
    }

    @Override
    protected SyncLastAccessedSubsectionResponse doInBackground(Object... params) {
        try {
            String courseId = (String)params[0];
            if(courseId!=null){
                IApi api = ApiFactory.getCacheApiInstance(context);
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
