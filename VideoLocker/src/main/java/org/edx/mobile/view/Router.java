package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.AppConstants;

/**
 * Created by aleffert on 1/30/15.
 */
public class Router {

    static private Router sInstance;

    // Note that this is not thread safe. The expectation is that this only happens
    // immediately when the app launches or synchronously at the start of a test.
    public static void setInstance(Router router) {
        sInstance = router;
    }

    public static Router getInstance() {
        return sInstance;
    }

    public void showDownloads(Activity sourceActivity) {
        Intent downloadIntent = new Intent(sourceActivity, DownloadListActivity.class);
        sourceActivity.startActivity(downloadIntent);
    }

    public void showFindCourses(Activity sourceActivity) {
        Intent findCoursesIntent = new Intent(sourceActivity, FindCoursesActivity.class);
        //Add this flag as multiple activities need to be created
        findCoursesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(findCoursesIntent);
    }

    public void showCourseInfo(Activity sourceActivity, String pathId) {
        Intent courseInfoIntent = new Intent(sourceActivity, CourseInfoActivity.class);
        courseInfoIntent.putExtra(CourseInfoActivity.EXTRA_PATH_ID, pathId);
        sourceActivity.startActivity(courseInfoIntent);
    }

    public void showMyVideos(Activity sourceActivity) {
        Intent myVideosIntent = new Intent(sourceActivity, MyVideosTabActivity.class);
        myVideosIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(myVideosIntent);
    }

    public void showMyGroups(Activity sourceActivity) {
        Intent myGroupsIntent = new Intent(sourceActivity, MyGroupsListActivity.class);
        myGroupsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sourceActivity.startActivity(myGroupsIntent);
    }

    public void showSettings(Activity sourceActivity) {
        Intent settingsIntent = new Intent(sourceActivity, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sourceActivity.startActivity(settingsIntent);
    }

    public void showLaunchScreen(Activity sourceActivity, boolean overrideAnimation) {
        Intent launchIntent = new Intent(sourceActivity, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.OVERRIDE_ANIMATION_FLAG,overrideAnimation);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sourceActivity.startActivity(launchIntent);
    }

    public void showLogin(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, LoginActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    public void showRegistration(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, RegisterActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    public void showMyCourses(Activity sourceActivity) {
        Intent intent = new Intent(sourceActivity, MyCoursesListActivity.class);
        /*
        Using CLEAR_TOP flag, causes the activity to be re-created every time.
        This reloads the list of courses. We don't want that.
        Using REORDER_TO_FRONT solves this problem
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(intent);

        // let login screens be ended
        Intent loginIntent = new Intent();
        loginIntent.setAction(AppConstants.USER_LOG_IN);
        sourceActivity.sendBroadcast(loginIntent);
    }

    public void showCourseDetailTabs(Activity activity, EnrolledCoursesResponse model,
                                     boolean announcements) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(CourseDetailTabActivity.EXTRA_ENROLLMENT, model);
        courseBundle.putBoolean(CourseDetailTabActivity.EXTRA_ANNOUNCEMENTS, announcements);

        Intent courseDetail = new Intent(activity, CourseDetailTabActivity.class);
        courseDetail.putExtra(CourseDetailTabActivity.EXTRA_BUNDLE, courseBundle);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);
    }
}
