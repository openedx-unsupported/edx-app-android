package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;

import org.edx.mobile.base.BaseFragmentActivity;

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

    public void showCourseInfo(Activity sourceActivity) {
        Intent courseInfoIntent = new Intent(sourceActivity, CourseInfoActivity.class);
        sourceActivity.startActivity(courseInfoIntent);
    }

    public void showMyVideos(Activity sourceActivity) {
        Intent myVideosIntent = new Intent(sourceActivity,
                MyVideosTabActivity.class);
        myVideosIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(myVideosIntent);
    }

}
