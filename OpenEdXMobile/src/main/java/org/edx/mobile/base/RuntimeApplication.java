package org.edx.mobile.base;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.room.Room;

import org.edx.mobile.data.LocalDataSource;
import org.edx.mobile.data.SubodhaDataBase;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.ExtensionRegistry;

import javax.inject.Inject;

/**
 * Put any custom application configuration here.
 * This file will not be edited by edX unless absolutely necessary.
 */
public class RuntimeApplication extends MainApplication {
   // private static LocalDataSource localData;
    @SuppressWarnings("unused")
    @Inject
    ExtensionRegistry extensionRegistry;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If you have any custom extensions, add them here. For example:
        // extensionRegistry.forType(SettingsExtension.class).add(new MyCustomSettingsExtension());
        MultiDex.install(this);
        /*localData = new LocalDataSource(Room.databaseBuilder(this, SubodhaDataBase.class, "subodha_database").fallbackToDestructiveMigration()
                .build());*/
    }

    /*public static LocalDataSource getLocalData() {
        return localData;
    }*/
}
