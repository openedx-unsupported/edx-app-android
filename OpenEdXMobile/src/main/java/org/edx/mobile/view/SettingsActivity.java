package org.edx.mobile.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

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
