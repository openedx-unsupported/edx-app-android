package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.http.HttpException;

public class LogoutTask extends Task<Void> {
    @Inject
    private LoginAPI loginAPI;

    @NonNull
    private final String refreshToken;

    public LogoutTask(@NonNull final Context context, @NonNull final String refreshToken) {
        super(context);
        this.refreshToken = refreshToken;
    }

    @Override
    public Void call() throws HttpException {
        loginAPI.logOut(refreshToken);
        return null;
    }
}
