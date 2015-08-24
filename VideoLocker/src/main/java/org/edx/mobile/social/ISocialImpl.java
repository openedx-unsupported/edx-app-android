package org.edx.mobile.social;

import android.app.Activity;
import android.os.Bundle;

public abstract class ISocialImpl implements ISocial {
    
    protected ISocial.Callback callback;
    protected Activity activity;
    
    public ISocialImpl(Activity activity) {
        this.activity = activity;
    }
    
    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        this.activity = null;
    }

    // Providing empty implementations of the callbacks so that
    // subclasses are not forced to implement all of them.
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
}
