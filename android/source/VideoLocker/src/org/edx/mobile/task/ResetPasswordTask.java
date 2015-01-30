package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.ResetPasswordResponse;

public abstract class ResetPasswordTask extends Task<ResetPasswordResponse> {

    public ResetPasswordTask(Context context) {
        super(context);
    }

    @Override
    protected ResetPasswordResponse doInBackground(Object... params) {
        try {
            String emailId = params[0].toString();
            if(emailId!=null){
                Api api = new Api(context);
                ResetPasswordResponse res = api.resetPassword(emailId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }
}
