package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

public abstract class ResetPasswordTask extends Task<ResetPasswordResponse> {

    public ResetPasswordTask(Context context) {
        super(context);
    }

    @Override
    protected ResetPasswordResponse doInBackground(Object... params) {
        try {
            String emailId = params[0].toString();
            if(emailId!=null){
                IApi api = ApiFactory.getCacheApiInstance(context);
                ResetPasswordResponse res = api.doResetPassword(emailId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
