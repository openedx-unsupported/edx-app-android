package org.edx.mobile.tta.task.profile;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.profile.UserAddressResponse;

public class GetUserAddressTask extends Task<UserAddressResponse> {

    private Bundle parameters;

    @Inject
    private LoginAPI loginAPI;

    public GetUserAddressTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public UserAddressResponse call() throws Exception {
        return loginAPI.getUserAddress(parameters);
    }
}
