package org.edx.mobile.tta.data.pref;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

public class AppPref {

    @NonNull
    private final PrefManager prefManager;

    public AppPref(@NonNull Context context) {
        prefManager = new PrefManager(context, PrefManager.Pref.APP);
    }

    public void setFirstLaunch(boolean firstLaunch){
        prefManager.put(PrefManager.Key.FIRST_LAUNCH, firstLaunch);
    }

    public boolean isFirstLaunch(){
        return prefManager.getBoolean(PrefManager.Key.FIRST_LAUNCH, true);
    }

    public boolean isFirstLogin(){
        return prefManager.getBoolean(PrefManager.Key.FIRST_LOGIN, true);
    }
}
