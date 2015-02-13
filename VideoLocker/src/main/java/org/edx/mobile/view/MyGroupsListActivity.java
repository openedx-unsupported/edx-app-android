package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

public class MyGroupsListActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureDrawer();
        setTitle(getString(R.string.label_my_groups));

    }

    @Override
    public Fragment getFirstFragment() {
        return new GroupsListFragment();
    }

}
