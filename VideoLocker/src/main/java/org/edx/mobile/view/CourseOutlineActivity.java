package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;


/**
 *
 */
public class CourseOutlineActivity extends CourseBaseActivity {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private CourseOutlineFragment fragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setApplyPrevTransitionOnRestart(true);
        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
      //  configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.course_outline));
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            try {

                fragment = new CourseOutlineFragment();
                fragment.setTaskProcessCallback(this);

                if (courseData != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Router.EXTRA_COURSE_OUTLINE, courseData);
                    fragment.setArguments(bundle);
                }
                //this activity will only ever hold this lone fragment, so we
                // can afford to retain the instance during activity recreation
                fragment.setRetainInstance(true);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.disallowAddToBackStack();
                fragmentTransaction.commit();

            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
