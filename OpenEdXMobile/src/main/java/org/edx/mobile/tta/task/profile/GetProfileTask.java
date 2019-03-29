package org.edx.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.task.Task;

public class GetProfileTask extends Task<ProfileModel> {

    @Inject
    private LoginAPI loginAPI;

    public GetProfileTask(Context context) {
        super(context);
    }

    @Override
    public ProfileModel call() throws Exception {
        return loginAPI.getProfile();
    }
}
