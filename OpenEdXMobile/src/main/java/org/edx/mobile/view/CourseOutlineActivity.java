package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;

import roboguice.inject.InjectExtra;

import static org.edx.mobile.view.Router.EXTRA_BUNDLE;
import static org.edx.mobile.view.Router.EXTRA_COURSE_COMPONENT_ID;
import static org.edx.mobile.view.Router.EXTRA_COURSE_DATA;
import static org.edx.mobile.view.Router.EXTRA_COURSE_UPGRADE_DATA;
import static org.edx.mobile.view.Router.EXTRA_IS_VIDEOS_MODE;
import static org.edx.mobile.view.Router.EXTRA_LAST_ACCESSED_ID;

public class CourseOutlineActivity extends BaseSingleFragmentActivity {
    @InjectExtra(EXTRA_BUNDLE)
    private Bundle courseBundle;

    @InjectExtra(value = EXTRA_COURSE_COMPONENT_ID, optional = true)
    private String courseComponentId = null;

    @InjectExtra(EXTRA_IS_VIDEOS_MODE)
    private boolean isVideoMode = false;

    public static Intent newIntent(Activity activity,
                                   EnrolledCoursesResponse courseData,
                                   CourseUpgradeResponse courseUpgradeData,
                                   String courseComponentId, String lastAccessedId,
                                   boolean isVideosMode) {
        final Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, courseData);
        courseBundle.putParcelable(EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData);
        courseBundle.putString(EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        final Intent intent = new Intent(activity, CourseOutlineActivity.class);
        intent.putExtra(EXTRA_BUNDLE, courseBundle);
        intent.putExtra(EXTRA_LAST_ACCESSED_ID, lastAccessedId);
        intent.putExtra(EXTRA_IS_VIDEOS_MODE, isVideosMode);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (courseComponentId == null) {
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) courseBundle.getSerializable(EXTRA_COURSE_DATA);
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

    public void onEvent(CourseUpgradedEvent event) {
        finish();
    }
}
