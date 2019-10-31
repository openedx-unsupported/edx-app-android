package org.humana.mobile.tta.firebase;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.task.Task;

public class FirebaseTokenUpdateTask extends Task<FirebaseUpdateTokenResponse> {

    @NonNull
    private Bundle parameters;

    @Inject
    private LoginAPI loginAPI;

    public FirebaseTokenUpdateTask(@NonNull Context context, @NonNull Bundle mxparameters) {
        super(context);
        this.parameters = mxparameters;
    }

    @Override
    @NonNull
    public FirebaseUpdateTokenResponse call() throws Exception {
        return loginAPI.updateFireBaseTokenToServer(parameters);
    }

}
