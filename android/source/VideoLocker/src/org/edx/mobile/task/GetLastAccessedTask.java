package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;

public abstract class GetLastAccessedTask extends Task<SyncLastAccessedSubsectionResponse> {

    public GetLastAccessedTask(Context context) {
        super(context);
    }

    @Override
    protected SyncLastAccessedSubsectionResponse doInBackground(Object... params) {
        try {
            String courseId = (String)params[0];
            if(courseId!=null){
                Api api = new Api(context);
                SyncLastAccessedSubsectionResponse res = api.getLastAccessedSubsection(courseId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }
}
