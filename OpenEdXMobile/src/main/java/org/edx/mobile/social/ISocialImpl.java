package org.edx.mobile.social;

import android.app.Activity;

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
}
