package org.humana.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.humana.mobile.base.BaseSingleFragmentActivity;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.module.analytics.Analytics;

import roboguice.inject.InjectExtra;

public class CourseOutlineActivity extends BaseSingleFragmentActivity {
    @InjectExtra(Router.EXTRA_BUNDLE)
    private Bundle courseBundle;

    @InjectExtra(value = Router.EXTRA_COURSE_COMPONENT_ID, optional = true)
    private String courseComponentId = null;

    @InjectExtra(value = Router.EXTRA_Unit_id, optional = true)
    private String unitId = null;

    @InjectExtra(value = Router.EXTRA_Unit_TYPE, optional = true)
    private String unitType = null;

    @InjectExtra(value = Router.EXTRA_TITLE, optional = true)
    private String unitTitle = null;

    @InjectExtra(value = Router.EXTRA_UNIT_DESC, optional = true)
    private String unitDesc = null;

    @InjectExtra(Router.EXTRA_IS_VIDEOS_MODE)
    private boolean isVideoMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (courseComponentId == null) {
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) courseBundle.getSerializable(Router.EXTRA_COURSE_DATA);
            environment.getAnalyticsRegistry().trackScreenView(
                    isVideoMode ? Analytics.Screens.VIDEOS_COURSE_VIDEOS : Analytics.Screens.COURSE_OUTLINE,
                    courseData.getCourse().getId(), null);

            setTitle(courseData.getCourse().getName());
        }
    }

    @Override
    public Fragment getFirstFragment() {
        final Fragment fragment = new CourseOutlineFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

}
