package org.edx.mobile.social;

import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;

import org.edx.mobile.logger.OEXLogger;

public interface ISocial extends ActivityLifecycleCallbacks {

    public final OEXLogger logger = new OEXLogger(ISocial.class.getName());
    
    void login();
    void logout();
    void setCallback(ISocial.Callback callback);
    void onActivityResult(int requestCode, int resultCode, Intent data);

    public static interface Callback {
        void onLogin(String accessToken);
    }
}
