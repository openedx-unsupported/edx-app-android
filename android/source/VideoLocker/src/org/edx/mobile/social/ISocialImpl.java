package org.edx.mobile.social;

import java.lang.ref.WeakReference;

import android.app.Activity;

public abstract class ISocialImpl implements ISocial {
    
    protected ISocial.Callback callback;
    protected WeakReference<Activity> activity;
    
    public ISocialImpl(Activity activity) {
        this.activity = new WeakReference<Activity>(activity);
    }
    
    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
