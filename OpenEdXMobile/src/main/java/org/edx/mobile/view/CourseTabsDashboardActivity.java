package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import static org.edx.mobile.view.Router.EXTRA_ANNOUNCEMENTS;
import static org.edx.mobile.view.Router.EXTRA_COURSE_DATA;

public class CourseTabsDashboardActivity extends BaseSingleFragmentActivity {
    public static Intent newIntent(@NonNull Activity activity,
                                   @NonNull EnrolledCoursesResponse courseData,
                                   boolean announcements) {
        Intent intent = new Intent(activity, CourseTabsDashboardActivity.class);
        intent.putExtra(EXTRA_COURSE_DATA, courseData);
        intent.putExtra(EXTRA_ANNOUNCEMENTS, announcements);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        return CourseTabsDashboardFragment.newInstance();
    }
}
