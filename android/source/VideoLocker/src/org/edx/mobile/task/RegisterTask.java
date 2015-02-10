package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.RegisterResponse;

public abstract class RegisterTask extends Task<RegisterResponse> {

    private Bundle parameters;

    public RegisterTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    protected RegisterResponse doInBackground(Object... params) {
        try {
            Api api = new Api(context);
            RegisterResponse res = api.register(parameters);
            return res;
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }
}
