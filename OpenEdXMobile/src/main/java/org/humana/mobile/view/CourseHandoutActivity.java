package org.humana.mobile.view;

import android.support.v4.app.Fragment;

import org.humana.mobile.R;
import org.humana.mobile.base.BaseSingleFragmentActivity;

public class CourseHandoutActivity extends BaseSingleFragmentActivity {
    private Fragment fragment;

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_handouts));
    }

    @Override
    public Fragment getFirstFragment() {
        return new CourseHandoutFragment();
    }
}
