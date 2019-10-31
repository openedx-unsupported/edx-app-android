package org.humana.mobile.whatsnew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.base.BaseAppActivity;
import org.humana.mobile.base.MainApplication;
import org.humana.mobile.module.prefs.PrefManager;

public class WhatsNewActivity extends BaseAppActivity {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, WhatsNewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_new);


        Fragment singleFragment = new WhatsNewFragment();

        // This activity will only ever hold this lone fragment, so we
        // can afford to retain the instance during activity recreation
        singleFragment.setRetainInstance(true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, singleFragment, null);
        fragmentTransaction.disallowAddToBackStack();
        fragmentTransaction.commit();

        final PrefManager.AppInfoPrefManager appPrefs = new PrefManager.AppInfoPrefManager(MainApplication.application);
        appPrefs.setWhatsNewShown(BuildConfig.VERSION_NAME);
    }
}
