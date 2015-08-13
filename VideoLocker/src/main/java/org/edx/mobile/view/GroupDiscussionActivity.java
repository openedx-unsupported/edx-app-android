package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

/**
 * Created by miankhalid on 8/13/15.
 */
public class GroupDiscussionActivity extends CourseBaseActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());

    private GroupDiscussionFragment fragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setApplyPrevTransitionOnRestart(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fragment = new GroupDiscussionFragment();

            fragment.setRetainInstance(true);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, fragment);
            fragmentTransaction.disallowAddToBackStack();
            fragmentTransaction.commit();
        }
    }

    protected boolean createOptionMenu(Menu menu) {
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected String getUrlForWebView() {
        return "";
    }
}
