package org.edx.mobile.authentication;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class DiscoveryTask extends Task<AuthResponseJwt> {

    @Inject
    private LoginAPI loginAPI;

    public DiscoveryTask(Context context) {
        super(context);
    }

    @Override
    @NonNull
    public AuthResponseJwt call() throws Exception {
        return loginAPI.getAccessJwt();
    }
}

