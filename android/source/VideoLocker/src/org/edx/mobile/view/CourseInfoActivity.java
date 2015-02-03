package org.edx.mobile.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.base.FindCoursesBaseActivity;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

public class CourseInfoActivity extends FindCoursesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find_course_info);
        super.onCreate(savedInstanceState);

        try{
            segIO.screenViewsTracking(ISegment.Values.COURSE_INFO_SCREEN);
        }catch(Exception e){
            logger.error(e);
        }
    }

}