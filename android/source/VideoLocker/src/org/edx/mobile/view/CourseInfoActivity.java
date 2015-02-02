package org.edx.mobile.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

public class CourseInfoActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_courses);

        try{
            segIO.screenViewsTracking(getString(R.string.find_courses_title));
        }catch(Exception e){
            logger.error(e);
        }

        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
        }else{
            AppConstants.offline_flag = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.find_courses_title));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);
        return true;
    }
}