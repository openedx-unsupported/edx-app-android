package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;

/**
 * @deprecated As of release v2.13, use {@link CourseTabsDashboardActivity} instead.
 */
@Deprecated
public class CourseDashboardActivity extends BaseVideosDownloadStateActivity {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private CourseDashboardFragment fragment;
    protected EnrolledCoursesResponse courseData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_dashboard);
        super.setToolbarAsActionBar();

        Bundle data = savedInstanceState != null ? savedInstanceState :
                getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        courseData = (EnrolledCoursesResponse) data.getSerializable(Router.EXTRA_COURSE_DATA);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        //  addDrawer();

        environment.getAnalyticsRegistry().trackScreenView(
                Analytics.Screens.COURSE_DASHBOARD, courseData.getCourse().getId(), null);

    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(courseData.getCourse().getName());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fragment = new CourseDashboardFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable(CourseDashboardFragment.CourseData, courseData);
            fragment.setArguments(bundle);

            //this activity will only ever hold this lone fragment, so we
            // can afford to retain the instance during activity recreation
            fragment.setRetainInstance(true);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, fragment);
            fragmentTransaction.disallowAddToBackStack();
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
    }
}
