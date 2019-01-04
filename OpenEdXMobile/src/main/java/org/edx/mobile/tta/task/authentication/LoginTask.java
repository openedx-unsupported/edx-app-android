package org.edx.mobile.tta.task.authentication;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;

public class LoginTask extends Task<AuthResponse> {

    private String number;
    private String password;

    @Inject
    LoginAPI loginAPI;

    public LoginTask(Context context, String number, String password) {
        super(context);
        this.number = number;
        this.password = password;
    }

    @Override
    public AuthResponse call() throws Exception {
        return loginAPI.logInUsingMobileNumber(number,password);
    }
}
