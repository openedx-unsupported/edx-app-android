package org.edx.mobile.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;


public class CourseAnnouncementsActivity extends BaseSingleFragmentActivity {

    @Inject
    CourseAPI api;

    private EnrolledCoursesResponse courseData;


    public static String TAG = CourseAnnouncementsActivity.class.getCanonicalName();

    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = savedInstanceState != null ? savedInstanceState :
                getIntent().getBundleExtra(Router.EXTRA_BUNDLE);

        courseData = (EnrolledCoursesResponse) bundle
                .getSerializable(Router.EXTRA_COURSE_DATA);

        //check courseData again, it may be fetched from local cache
        if (courseData != null) {
            activityTitle = courseData.getCourse().getName();
            environment.getAnalyticsRegistry().trackScreenView(
                    Analytics.Screens.COURSE_ANNOUNCEMENTS,
                    courseData.getCourse().getId(),
                    null);
        } else {

            boolean handleFromNotification = handleIntentFromNotification();
            //this is not from notification
            if (!handleFromNotification) {
                //it is a good idea to go to the my course page. as loading of my courses
                //take a while to load. that the only way to get anouncement link
                environment.getRouter().showMainDashboard(this);
                finish();
            }
        }

    }

    /**
     * @return <code>true</code> if handle intent from notification successfully
     */
    private boolean handleIntentFromNotification() {
        if (bundle != null) {
            String courseId = bundle.getString(Router.EXTRA_COURSE_ID);
            //this is from notification
            if (!TextUtils.isEmpty(courseId)) {
                try {
                    bundle.remove(Router.EXTRA_COURSE_ID);
                    courseData = api.getCourseById(courseId);
                    if (courseData != null && courseData.getCourse() != null) {
                        bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
                        activityTitle = courseData.getCourse().getName();
                        return true;
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleIntentFromNotification();
        invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public Fragment getFirstFragment() {
        CourseAnnouncementsFragment fragment = new CourseAnnouncementsFragment();

        if (courseData != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
            fragment.setArguments(bundle);

        }
        return fragment;
    }
}
