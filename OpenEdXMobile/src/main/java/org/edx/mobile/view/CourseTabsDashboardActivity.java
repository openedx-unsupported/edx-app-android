package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import static org.edx.mobile.view.Router.EXTRA_ANNOUNCEMENTS;
import static org.edx.mobile.view.Router.EXTRA_COURSE_DATA;
import static org.edx.mobile.view.Router.EXTRA_COURSE_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class CourseTabsDashboardActivity extends OfflineSupportBaseActivity {
    public static Intent newIntent(@NonNull Activity activity,
                                   @Nullable EnrolledCoursesResponse courseData,
                                   @Nullable String courseId, boolean announcements,
                                   @Nullable @ScreenDef String screenName) {
        Intent intent = new Intent(activity, CourseTabsDashboardActivity.class);
        intent.putExtra(EXTRA_COURSE_DATA, courseData);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_ANNOUNCEMENTS, announcements);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        return CourseTabsDashboardFragment.newInstance(
                (EnrolledCoursesResponse) getIntent().getSerializableExtra(EXTRA_COURSE_DATA),
                getIntent().getStringExtra(EXTRA_COURSE_ID),
                getIntent().getStringExtra(EXTRA_SCREEN_NAME));
    }

    @Override
    public Object getRefreshEvent() {
        return new CourseDashboardRefreshEvent();
    }
}
