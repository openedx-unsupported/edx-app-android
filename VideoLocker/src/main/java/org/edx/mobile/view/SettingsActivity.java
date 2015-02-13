package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

public class SettingsActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.settings_txt));
        configureDrawer();
    }

    @Override
    public Fragment getFirstFragment() {
        return new SettingsFragment();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);

        return true;

    }

}
