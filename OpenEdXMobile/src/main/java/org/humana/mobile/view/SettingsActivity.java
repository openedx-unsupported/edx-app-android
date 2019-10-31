package org.humana.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.humana.mobile.R;
import org.humana.mobile.base.BaseSingleFragmentActivity;

public class SettingsActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.settings_txt));
    }

    @Override
    public Fragment getFirstFragment() {
        return new SettingsFragment();
    }

}
