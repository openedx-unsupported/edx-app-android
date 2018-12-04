package org.edx.mobile.tta.data.pref;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class AppPref {

    public static class Provider implements com.google.inject.Provider<AppPref>{

        @Inject
        Context context;

        @Override
        public AppPref get() {
            Log.d("__________LOG_________", "app ref");
            return new AppPref(context);
        }
    }

    @NonNull
    private final PrefManager prefManager;

    public AppPref(Context context) {
        prefManager = new PrefManager(context, PrefManager.Pref.APP);
    }

    public void setFirstLaunch(boolean firstLaunch){
        prefManager.put(PrefManager.Key.FIRST_LAUNCH, firstLaunch);
    }

    public boolean isFirstLaunch(){
        return prefManager.getBoolean(PrefManager.Key.FIRST_LAUNCH, true);
    }
}
