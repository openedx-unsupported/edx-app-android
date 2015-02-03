package org.edx.mobile.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

public class FindCoursesActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_courses);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.find_courses_title));
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.find_courses_title));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);
        return true;
    }
}