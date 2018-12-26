package org.edx.mobile.tta.data;

import android.content.Context;

import roboguice.RoboGuice;

public abstract class BaseRoboInjector {
    public BaseRoboInjector(Context mCtx)
    {
        RoboGuice.getInjector(mCtx).injectMembersWithoutViews(this);
    }
}
