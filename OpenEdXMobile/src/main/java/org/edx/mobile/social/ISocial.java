package org.edx.mobile.social;

import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.edx.mobile.logger.Logger;

public interface ISocial extends ActivityLifecycleCallbacks {

    Logger logger = new Logger(ISocial.class.getName());

    void login();
    void logout();
    void setCallback(ISocial.Callback callback);
    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    interface Callback {
        void onLogin(String accessToken);

        default void onCancel() {
        }

        default void onError(@Nullable Exception exception) {
        }
    }
}
