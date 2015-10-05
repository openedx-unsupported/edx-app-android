package org.edx.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.task.Task;

import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class GetProfileFormDescriptionTask extends
        Task<RegistrationDescription> {


    public GetProfileFormDescriptionTask(@NonNull Context context) {
        super(context);
    }


    public RegistrationDescription call() throws Exception {
        try (InputStream in = context.getAssets().open("config/profiles.json")) {
            return new Gson().fromJson(new InputStreamReader(in), RegistrationDescription.class);
        }
    }
}
