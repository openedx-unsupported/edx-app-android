package org.edx.mobile.social.facebook;

import android.app.Activity;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.edx.mobile.social.ISocialImpl;

import java.util.Collections;

public class FacebookAuth extends ISocialImpl {
    private final CallbackManager callbackManager;

    public FacebookAuth(Activity activity) {
        super(activity);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if (callback != null) {
                            callback.onLogin(loginResult.getAccessToken().getToken());
                        }
                        logger.debug("Facebook Logged in successfully.");
                    }

                    @Override
                    public void onCancel() {
                        logger.debug("Facebook Log in canceled.");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        logger.error(error);
                    }
                });
    }

    @Override
    public void login() {
        LoginManager.getInstance().logInWithReadPermissions(activity, Collections.singletonList("email"));
    }

    @Override
    public void logout() {
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
