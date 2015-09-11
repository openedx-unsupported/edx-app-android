package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;


/**
 * TODO - it is just a place holder for now. as we need to use it
 * to navigation to new views.
 */
public class CourseDashboardActivity extends BaseFragmentActivity {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private CourseDashboardFragment fragment;
    protected EnrolledCoursesResponse courseData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setApplyPrevTransitionOnRestart(true);

        Bundle data = savedInstanceState != null ? savedInstanceState :
                getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        courseData = (EnrolledCoursesResponse) data.getSerializable(Router.EXTRA_ENROLLMENT);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
      //  configureDrawer();

        try{
            environment.getSegment().screenViewsTracking(getString(R.string.course_home));
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(courseData.getCourse().getName());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            fragment = new CourseDashboardFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable(CourseDashboardFragment.CourseData, courseData);
            fragment.setArguments(bundle);

            //this activity will only ever hold this lone fragment, so we
            // can afford to retain the instance during activity recreation
            fragment.setRetainInstance(true);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content, fragment);
            fragmentTransaction.disallowAddToBackStack();
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
    }
}
