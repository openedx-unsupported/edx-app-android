package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.services.ServiceManager;

public abstract class ResetPasswordTask extends Task<ResetPasswordResponse> {

    String emailId;
    public ResetPasswordTask(Context context,String emailId) {
        super(context);
        this.emailId = emailId;
    }

    @Override
    public ResetPasswordResponse call() throws Exception{
        try {

            if(emailId!=null){
                ServiceManager api = environment.getServiceManager();
                ResetPasswordResponse res = api.resetPassword(emailId);
                return res;
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
