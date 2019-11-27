package org.edx.mobile.authentication;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class LoginTask extends Task<AuthResponse> {

    @NonNull
    private final String username;
    @NonNull
    private final String password;

    @Inject
    private LoginAPI loginAPI;

    public LoginTask(@NonNull Context context, @NonNull String username, @NonNull String password) {
        super(context);
        this.username = username;
        this.password = password;
    }

    @Override
    @NonNull
    public AuthResponse call() throws Exception {
        return loginAPI.logInUsingEmail(username, password);
    }
}
