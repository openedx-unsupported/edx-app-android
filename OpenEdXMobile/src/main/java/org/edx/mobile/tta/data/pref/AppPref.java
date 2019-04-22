package org.edx.mobile.tta.data.pref;

import android.content.Context;
import android.support.annotation.NonNull;
import org.edx.mobile.module.prefs.PrefManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppPref {

    @NonNull
    private final PrefManager prefManager;

    @Inject
    public AppPref(@NonNull Context context) {
        prefManager = new PrefManager(context, PrefManager.Pref.APP_INFO);
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

    public void setCurrentBreadcrumb(String breadcrumb){
        prefManager.put(PrefManager.Key.CURRENT_BREADCRUMB, breadcrumb);
    }

    public String getCurrentBreadcrumb(){
        String breadcrumb = prefManager.getString(PrefManager.Key.CURRENT_BREADCRUMB);
        return breadcrumb == null ? "" : breadcrumb;
    }
}
