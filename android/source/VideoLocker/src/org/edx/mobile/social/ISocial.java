package org.edx.mobile.social;

import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;

public interface ISocial extends ActivityLifecycleCallbacks {

    public static final String TAG = "Social";
    
    void login();
    void logout();
    void setCallback(ISocial.Callback callback);
    void onActivityResult(int requestCode, int resultCode, Intent data);

    public static interface Callback {
        void onLogin(String accessToken);
    }
}
