package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.ISequential;


/**
 *
 */
public class CourseSequentialOutlineActivity extends CourseBaseActivity {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private CourseSequentialOutlineFragment fragment;
    private ISequential sequential;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setApplyPrevTransitionOnRestart(true);
        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
       // configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.course_outline));
        }catch(Exception e){
            logger.error(e);
        }

    }

    protected void initialize(Bundle arg){
        super.initialize(arg);
        sequential = (ISequential) bundle.getSerializable(Router.EXTRA_SEQUENTIAL);
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

                fragment = new CourseSequentialOutlineFragment();
                fragment.setTaskProcessCallback(this);

                if (courseData != null &&  sequential != null ) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Router.EXTRA_COURSE_OUTLINE, courseData);
                    bundle.putSerializable(Router.EXTRA_SEQUENTIAL, sequential);
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

    @Override
    protected boolean createOptionMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.course_detail, menu);
        return true;
    }
}
