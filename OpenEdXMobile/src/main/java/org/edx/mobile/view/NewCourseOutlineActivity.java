package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;

import roboguice.inject.InjectExtra;


/**
 * Top level outline for the Course
 */
public class NewCourseOutlineActivity extends BaseSingleFragmentActivity {
    @InjectExtra(Router.EXTRA_BUNDLE)
    private Bundle courseBundle;

    @InjectExtra(value = Router.EXTRA_COURSE_COMPONENT_ID, optional = true)
    private String courseComponentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We don't need the drawer here
        blockDrawerFromOpening();

        if (courseComponentId == null) {
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) courseBundle.getSerializable(Router.EXTRA_COURSE_DATA);
            environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_OUTLINE,
                    courseData.getCourse().getId(), null);

            setTitle(courseData.getCourse().getName());
        }
    }

    @Override
    public Fragment getFirstFragment() {
        final Fragment fragment = new NewCourseOutlineFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
