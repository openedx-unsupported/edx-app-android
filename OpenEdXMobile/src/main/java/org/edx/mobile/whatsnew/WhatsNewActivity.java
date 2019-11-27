package org.edx.mobile.whatsnew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.module.prefs.PrefManager;

public class WhatsNewActivity extends BaseFragmentActivity {

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
