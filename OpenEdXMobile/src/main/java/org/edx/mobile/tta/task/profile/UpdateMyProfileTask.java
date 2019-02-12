package org.edx.mobile.tta.task.profile;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.profile.UpdateMyProfileResponse;

public class UpdateMyProfileTask extends Task<UpdateMyProfileResponse> {

    private Bundle parameters;

    private String username;

    @Inject
    private LoginAPI loginAPI;

    public UpdateMyProfileTask(Context context, Bundle parameters, String username) {
        super(context);
        this.parameters = parameters;
        this.username = username;
    }

    @Override
    public UpdateMyProfileResponse call() throws Exception {
        return loginAPI.updateMyProfile(parameters,username);
    }
}
