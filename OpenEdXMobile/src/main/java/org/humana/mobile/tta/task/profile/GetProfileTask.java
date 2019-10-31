package org.humana.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.model.api.ProfileModel;
import org.humana.mobile.task.Task;

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
