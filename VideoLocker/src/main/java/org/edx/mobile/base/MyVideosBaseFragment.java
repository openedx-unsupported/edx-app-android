package org.edx.mobile.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.util.AppConstants;

public abstract class MyVideosBaseFragment extends Fragment {
    protected IDatabase db;
    protected IStorage storage;
    protected ISegment segIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDB();
        enableDownloadCompleteCallback();
    }

    @Override
    public void onStop() {
        super.onStop();
        disableDownloadCompleteCallback();
    }

    private void initDB() {
        storage = new Storage(getActivity());

        UserPrefs userprefs = new UserPrefs(getActivity());
        String username = null;
        if (userprefs != null) {
            ProfileModel profile = userprefs.getProfile();
            if(profile!=null){
                username =profile.username;
            }
        }
        db = DatabaseFactory.getInstance(getActivity(), 
                DatabaseFactory.TYPE_DATABASE_NATIVE, username);
        
        segIO = SegmentFactory.getInstance();
    }

    /**
     * Call this function when Video completes downloading
     * so that downloaded videos appears in MyVideos listing
     */
    public abstract void reloadList();

    //Broadcast Receiver to notify all activities to finish if user logs out
    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadList();
        }
    };

    protected void enableDownloadCompleteCallback() {
        try {
            // Register for Download Complete notification
            IntentFilter filter = new IntentFilter();
            filter.addAction(AppConstants.DOWNLOAD_COMPLETE);
            getActivity().registerReceiver(downloadCompleteReceiver, filter);
        }catch(Exception e){

        }
    }

    protected void disableDownloadCompleteCallback() {
        try{
            // un-register Download Complete receiver
            getActivity().unregisterReceiver(downloadCompleteReceiver);
        }catch(Exception e){

        }
    }
}
