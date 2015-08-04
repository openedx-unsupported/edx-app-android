package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.CourseTopics;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;

import de.greenrobot.event.EventBus;

/**
 * Created by aleffert on 1/30/15.
 */
@Singleton
public class Router {

    public static final String EXTRA_ANNOUNCEMENTS = "announcements";
    public static final String EXTRA_BUNDLE = "bundle";
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_ENROLLMENT = "enrollment";
    public static final String EXTRA_SEQUENTIAL = "sequential";
    public static final String EXTRA_COURSE_UNIT = "course_unit";
    public static final String EXTRA_COURSE_COMPONENT_ID = "course_component_id";
    public static final String EXTRA_COURSE_DATA = "course_data";


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

    public void showLaunchScreen(Context context, boolean overrideAnimation) {
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.OVERRIDE_ANIMATION_FLAG, overrideAnimation);
        if ( context instanceof  Activity)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        else
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }

    public void showLogin(Context context) {
        Intent launchIntent = new Intent(context, LoginActivity.class);
        if ( !(context instanceof  Activity) )
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
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

    public void showCourseDetailTabs(Activity activity, Config config, EnrolledCoursesResponse model,
                                     boolean announcements) {

        if ( config.isNewCourseNavigationEnabled() ){
            showCourseDashboard(activity, model, announcements);
            return;
        }

        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_ENROLLMENT, model);
        courseBundle.putBoolean(EXTRA_ANNOUNCEMENTS, announcements);

        Intent courseDetail = new Intent(activity, CourseDetailTabActivity.class);
        courseDetail.putExtra( EXTRA_BUNDLE, courseBundle);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);
    }

    /**
     * FIXME - it will bring to different view in the future
     * @param activity
     * @param model
     */
    public void showCourseAnnouncement(Activity activity, Config config,  EnrolledCoursesResponse model ) {

        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_ENROLLMENT, model);
        courseBundle.putBoolean(EXTRA_ANNOUNCEMENTS, true);


        Intent courseDetail;
        if ( config.isNewCourseNavigationEnabled() )
            courseDetail = new Intent(activity, CourseDetailInfoActivity.class);
        else
            courseDetail = new Intent(activity, CourseDetailTabActivity.class);
        courseDetail.putExtra( EXTRA_BUNDLE, courseBundle);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);
    }

    public void showCourseContainerOutline(Activity activity, EnrolledCoursesResponse model, String courseComponentId) {

        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_ENROLLMENT, model);
        courseBundle.putString(EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        Intent courseDetail = new Intent(activity, CourseOutlineActivity.class);
        courseDetail.putExtra( EXTRA_BUNDLE, courseBundle);
        //TODO - what's the most suitable FLAG?
       // courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);
    }



    public void showCourseUnitDetail(Activity activity, EnrolledCoursesResponse model,
                                     String courseId,  CourseComponent unit ) {

        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_ENROLLMENT, model);
        courseBundle.putSerializable(EXTRA_COURSE_COMPONENT_ID, courseId);
        courseBundle.putSerializable(EXTRA_COURSE_UNIT, unit);

        Intent courseDetail = new Intent(activity, CourseUnitNavigationActivity.class);
        courseDetail.putExtra( EXTRA_BUNDLE, courseBundle);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);
    }


    public void showCourseDashboard(Activity activity, EnrolledCoursesResponse model,
                                     boolean announcements) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_ENROLLMENT, model);
        courseBundle.putBoolean(EXTRA_ANNOUNCEMENTS, announcements);

        Intent courseDetail = new Intent(activity, CourseDashboardActivity.class);
        courseDetail.putExtra( EXTRA_BUNDLE, courseBundle);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);

    }

    /**
     *  this method can be called either through UI [ user clicks LOGOUT button],
     *  or programmatically
     */
    @Inject
    public void forceLogout(Context context, ISegment segment, NotificationDelegate delegate){
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.clearAuth();
        pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, "none");

        EventBus.getDefault().post(new LogoutEvent());

        segment.trackUserLogout();
        segment.resetIdentifyUser();

        delegate.unsubscribeAll();

        showLaunchScreen(context, true);
        showLogin(context);
    }

    public void showHandouts(Activity activity, EnrolledCoursesResponse courseData) {
        Intent handoutIntent = new Intent(activity, CourseHandoutActivity.class);
        handoutIntent.putExtra(CourseHandoutFragment.ENROLLMENT, courseData);
        handoutIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(handoutIntent);
    }

    public void showDiscussionTopics(Activity activity, final EnrolledCoursesResponse courseData) {
        // TODO: create a new DiscussionTopicsActivity and populate it with API result
//        Intent discussionTopicsIntent = new Intent(activity, DiscussionTopicsActivity.class);
//        discussionTopicsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        activity.startActivity(discussionTopicsIntent);


        new DiscussionAPI().getTopicList(courseData.getCourse().getId(), new APICallback<CourseTopics>() {
            @Override
            public void success(CourseTopics courseTopics) {
                System.out.println(courseTopics);

                // "unread", "last_activity_at",
                new DiscussionAPI().getThreadList(courseData.getCourse().getId(), courseTopics.getCoursewareTopics().get(0).getChildren().get(0).getIdentifier(),
                        "", "", new APICallback<TopicThreads>() {

                    @Override
                    public void success(TopicThreads threads) {
                        System.out.println(threads);
                    }
                    @Override
                    public void failure(Exception e){
                    }
                });
            }

            @Override
            public void failure(Exception e){
            }
        });
    }
}
