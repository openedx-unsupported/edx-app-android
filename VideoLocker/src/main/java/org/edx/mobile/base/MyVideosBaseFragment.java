package org.edx.mobile.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.view.MyVideosTabActivity;


public class MyVideosBaseFragment extends Fragment {
    public MyVideosTabActivity mActivity;
    protected IDatabase db;
    protected IStorage storage;
    protected ISegment segIO;
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MyVideosTabActivity) this.getActivity();
        initDB();
    }
    
    public boolean onBackPressed(){
        return false;
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

}
