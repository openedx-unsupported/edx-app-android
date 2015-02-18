package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;

public class CreateGroupActivity extends BaseSingleFragmentActivity {

    public static String TAG = CreateGroupActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay_put);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.label_new_group));
    }

    @Override
    public Fragment getFirstFragment() {
        return new CreateGroupFragment();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_put, R.anim.slide_out_bottom);
    }

}
