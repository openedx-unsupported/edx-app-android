package org.edx.mobile.app;

import android.app.Application;

import androidx.multidex.MultiDex;
import androidx.room.Room;

import org.edx.mobile.data.LocalDataSource;
import org.edx.mobile.data.SubodhaDataBase;

public class App extends Application {
    private static LocalDataSource localData;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        localData = new LocalDataSource(Room.databaseBuilder(this, SubodhaDataBase.class, "subodha_database").fallbackToDestructiveMigration()
                .build());
    }

    public static LocalDataSource getLocalData() {
        return localData;
    }
}
