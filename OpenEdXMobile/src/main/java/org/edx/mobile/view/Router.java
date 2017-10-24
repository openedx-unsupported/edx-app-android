package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.TaskStackBuilder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.profiles.UserProfileActivity;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.SecurityUtil;
import org.edx.mobile.view.dialog.WebViewActivity;
import org.edx.mobile.whatsnew.WhatsNewActivity;

import de.greenrobot.event.EventBus;

@Singleton
public class Router {
    public static final String EXTRA_ANNOUNCEMENTS = "announcements";
    public static final String EXTRA_BUNDLE = "bundle";
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_DETAIL = "course_detail";
    public static final String EXTRA_COURSE_DATA = "course_data";
    public static final String EXTRA_COURSE_UNIT = "course_unit";
    public static final String EXTRA_COURSE_COMPONENT_ID = "course_component_id";
    public static final String EXTRA_LAST_ACCESSED_ID = "last_accessed_id";
    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_DISCUSSION_TOPIC = "discussion_topic";
    public static final String EXTRA_DISCUSSION_THREAD = "discussion_thread";
    public static final String EXTRA_DISCUSSION_COMMENT = "discussion_comment";
    public static final String EXTRA_DISCUSSION_TOPIC_ID = "discussion_topic_id";
    public static final String EXTRA_IS_VIDEOS_MODE = "videos_mode";
    public static final String EXTRA_IS_ON_COURSE_OUTLINE = "is_on_course_outline";

    @Inject
    Config config;

    @Inject
    private LoginAPI loginAPI;
    @Inject
    private LoginPrefs loginPrefs;
    @Inject
    private IStorage storage;

    public void showDownloads(Activity sourceActivity) {
        Intent downloadIntent = new Intent(sourceActivity, DownloadListActivity.class);
        sourceActivity.startActivity(downloadIntent);
    }

    public void showDownloads(Context context) {
        Intent downloadIntent = new Intent(context, DownloadListActivity.class);
        downloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(downloadIntent);
    }

    public void showCourseInfo(Activity sourceActivity, String pathId) {
        Intent courseInfoIntent = new Intent(sourceActivity, CourseInfoActivity.class);
        courseInfoIntent.putExtra(CourseInfoActivity.EXTRA_PATH_ID, pathId);
        sourceActivity.startActivity(courseInfoIntent);
    }

    public void showSettings(Activity sourceActivity) {
        Intent settingsIntent = new Intent(sourceActivity, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sourceActivity.startActivity(settingsIntent);
    }

    public void showLaunchScreen(Context context) {
        final Intent launchIntent = new Intent(context,
                config.isNewLogistrationEnabled()
                        ? DiscoveryLaunchActivity.class
                        : LaunchActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(launchIntent);
    }

    public void showSplashScreen(Context context) {
        final Intent launchIntent = new Intent(context, SplashActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(launchIntent);
    }

    @NonNull
    public Intent getLogInIntent() {
        return LoginActivity.newIntent();
    }

    @NonNull
    public Intent getRegisterIntent() {
        return RegisterActivity.newIntent();
    }

    public void showMyCourses(Activity sourceActivity) {
        sourceActivity.startActivity(MyCoursesListActivity.newIntent());
    }

    public void showCourseDashboardTabs(Activity activity, Config config, EnrolledCoursesResponse model,
                                        boolean announcements) {

        showCourseDashboard(activity, model, announcements);
    }

    /**
     * FIXME - it will bring to different view in the future
     *
     * @param activity
     * @param model
     */
    public void showCourseAnnouncement(Activity activity, EnrolledCoursesResponse model) {
        final Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, model);
        courseBundle.putBoolean(EXTRA_ANNOUNCEMENTS, true);
        final Intent courseDetail = new Intent(activity, CourseAnnouncementsActivity.class);
        courseDetail.putExtra(EXTRA_BUNDLE, courseBundle);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDetail);
    }

    public void showCourseAnnouncementFromNotification(@NonNull Context context, @NonNull String courseId) {
        final Bundle courseBundle = new Bundle();
        courseBundle.putBoolean(Router.EXTRA_ANNOUNCEMENTS, true);
        courseBundle.putString(Router.EXTRA_COURSE_ID, courseId);
        final Intent courseDetail = new Intent(context, CourseAnnouncementsActivity.class).putExtra(EXTRA_BUNDLE, courseBundle);
        // TODO: It's not essential, but we may want additional activities on the back-stack (e.g. CourseDashboardActivity)
        TaskStackBuilder.create(context)
                .addNextIntent(courseDetail)
                .startActivities();
    }

    public void showCourseContainerOutline(Activity activity, EnrolledCoursesResponse model) {
        showCourseContainerOutline(activity, model, null);
    }

    public void showCourseContainerOutline(Activity activity, EnrolledCoursesResponse model,
                                           boolean isVideosMode) {
        showCourseContainerOutline(activity, -1, model, null, null, isVideosMode);
    }

    public void showCourseContainerOutline(Activity activity, EnrolledCoursesResponse model,
                                           String courseComponentId) {
        showCourseContainerOutline(activity, -1, model, courseComponentId, null, false);
    }

    public void showCourseContainerOutline(Activity activity, int requestCode,
                                           EnrolledCoursesResponse model, String courseComponentId,
                                           String lastAccessedId, boolean isVideosMode) {
        Intent courseDetail = createCourseOutlineIntent(activity, model, courseComponentId,
                lastAccessedId, isVideosMode);
        //TODO - what's the most suitable FLAG?
        // courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivityForResult(courseDetail, requestCode);
    }

    public void showCourseContainerOutline(Fragment fragment, int requestCode,
                                           EnrolledCoursesResponse model, String courseComponentId,
                                           String lastAccessedId, boolean isVideosMode) {
        Intent courseDetail = createCourseOutlineIntent(fragment.getActivity(), model,
                courseComponentId, lastAccessedId, isVideosMode);
        //TODO - what's the most suitable FLAG?
        // courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(courseDetail, requestCode);
    }

    private Intent createCourseOutlineIntent(Activity activity, EnrolledCoursesResponse model,
                                             String courseComponentId, String lastAccessedId,
                                             boolean isVideosMode) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, model);
        courseBundle.putString(EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        Intent courseDetail = new Intent(activity, CourseOutlineActivity.class);
        courseDetail.putExtra(EXTRA_BUNDLE, courseBundle);
        courseDetail.putExtra(EXTRA_LAST_ACCESSED_ID, lastAccessedId);
        courseDetail.putExtra(EXTRA_IS_VIDEOS_MODE, isVideosMode);

        return courseDetail;
    }

    public void showCourseUnitDetail(Fragment fragment, int requestCode, EnrolledCoursesResponse model,
                                     String courseComponentId, boolean isVideosMode) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, model);
        courseBundle.putSerializable(EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        Intent courseDetail = new Intent(fragment.getActivity(), CourseUnitNavigationActivity.class);
        courseDetail.putExtra(EXTRA_BUNDLE, courseBundle);
        courseDetail.putExtra(EXTRA_IS_VIDEOS_MODE, isVideosMode);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(courseDetail, requestCode);
    }


    public void showCourseDashboard(Activity activity, EnrolledCoursesResponse model,
                                    boolean announcements) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, model);
        courseBundle.putBoolean(EXTRA_ANNOUNCEMENTS, announcements);

        Intent courseDashboard = new Intent(activity, CourseDashboardActivity.class);
        courseDashboard.putExtra(EXTRA_BUNDLE, courseBundle);
        courseDashboard.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(courseDashboard);

    }

    public void showCourseDiscussionTopics(Activity activity, EnrolledCoursesResponse courseData) {
        Intent showDiscussionsIntent = new Intent(activity, CourseDiscussionTopicsActivity.class);
        showDiscussionsIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        showDiscussionsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(showDiscussionsIntent);
    }

    public void showCourseDiscussionAddPost(@NonNull Activity activity, @Nullable DiscussionTopic discussionTopic, @NonNull EnrolledCoursesResponse courseData) {
        Intent addPostIntent = new Intent(activity, DiscussionAddPostActivity.class);
        addPostIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        addPostIntent.putExtra(EXTRA_DISCUSSION_TOPIC, discussionTopic);
        addPostIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(addPostIntent);
    }

    public void showCourseDiscussionComments(Context context, DiscussionComment comment, DiscussionThread discussionThread, EnrolledCoursesResponse courseData) {
        Intent commentListIntent = new Intent(context, CourseDiscussionCommentsActivity.class);
        commentListIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        commentListIntent.putExtra(Router.EXTRA_DISCUSSION_COMMENT, comment);
        commentListIntent.putExtra(Router.EXTRA_DISCUSSION_THREAD, discussionThread);
        commentListIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(commentListIntent);
    }

    public void showCourseDiscussionPostsForSearchQuery(Activity activity, String query, EnrolledCoursesResponse courseData) {
        Intent showDiscussionPostsIntent = new Intent(activity, CourseDiscussionPostsActivity.class);
        showDiscussionPostsIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        showDiscussionPostsIntent.putExtra(EXTRA_SEARCH_QUERY, query);
        showDiscussionPostsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(showDiscussionPostsIntent);
    }

    public void showCourseDiscussionPostsForDiscussionTopic(Activity activity, DiscussionTopic topic, EnrolledCoursesResponse courseData) {
        Intent showDiscussionPostsIntent = new Intent(activity, CourseDiscussionPostsActivity.class);
        showDiscussionPostsIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        showDiscussionPostsIntent.putExtra(EXTRA_DISCUSSION_TOPIC, topic);
        showDiscussionPostsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(showDiscussionPostsIntent);
    }

    public void showCourseDiscussionResponses(Context context, DiscussionThread discussionThread, EnrolledCoursesResponse courseData) {
        Intent discussionResponsesIntent = new Intent(context, CourseDiscussionResponsesActivity.class);
        discussionResponsesIntent.putExtra(EXTRA_DISCUSSION_THREAD, discussionThread);
        discussionResponsesIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        discussionResponsesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(discussionResponsesIntent);
    }

    public void showCourseDiscussionAddResponse(Context context, DiscussionThread discussionThread) {
        Intent addResponseIntent = new Intent(context, DiscussionAddResponseActivity.class);
        addResponseIntent.putExtra(EXTRA_DISCUSSION_THREAD, discussionThread);
        addResponseIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(addResponseIntent);
    }

    public void showCourseDiscussionAddComment(Context context, DiscussionComment discussionComment, DiscussionThread discussionThread) {
        Intent addResponseIntent = new Intent(context, DiscussionAddCommentActivity.class);
        addResponseIntent.putExtra(EXTRA_DISCUSSION_THREAD, discussionThread);
        addResponseIntent.putExtra(EXTRA_DISCUSSION_COMMENT, discussionComment);
        addResponseIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(addResponseIntent);
    }

    /**
     * Clear the login data and exit to the splash screen. This should only be called internally;
     * for handling manual logout,
     * {@link #performManualLogout(Context, AnalyticsRegistry, NotificationDelegate)} should be used instead.
     *
     * @param context  The context.
     * @param analyticsRegistry  The analytics provider object.
     * @param delegate The notification delegate.
     * @see #performManualLogout(Context, AnalyticsRegistry, NotificationDelegate)
     */
    public void forceLogout(Context context, AnalyticsRegistry analyticsRegistry, NotificationDelegate delegate) {
        loginPrefs.clear();

        EventBus.getDefault().post(new LogoutEvent());

        analyticsRegistry.trackUserLogout();
        analyticsRegistry.resetIdentifyUser();

        delegate.unsubscribeAll();

        showSplashScreen(context);
    }

    /**
     * Clears all the user data, revokes the refresh and access tokens, and exit to the splash
     * screen. This should only be called in response to manual logout by the user; for performing
     * logout internally (e.g. in response to refresh token expiration),
     * {@link #forceLogout(Context, AnalyticsRegistry, NotificationDelegate)} should be used instead.
     *
     * @param context  The context.
     * @param analyticsRegistry  The analytics provider object.
     * @param delegate The notification delegate.
     * @see #forceLogout(Context, AnalyticsRegistry, NotificationDelegate)
     */
    public void performManualLogout(Context context, AnalyticsRegistry analyticsRegistry, NotificationDelegate delegate) {
        // Remove all ongoing downloads first which requires username
        storage.removeAllDownloads();
        loginAPI.logOut();
        forceLogout(context, analyticsRegistry, delegate);
        SecurityUtil.clearUserData(context);
    }

    public void showHandouts(Activity activity, EnrolledCoursesResponse courseData) {
        Intent handoutIntent = new Intent(activity, CourseHandoutActivity.class);
        handoutIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        handoutIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(handoutIntent);
    }

    public void showUserProfile(@NonNull Context context, @NonNull String username) {
        context.startActivity(UserProfileActivity.newIntent(context, username, false));
    }

    public void showUserProfileWithNavigationDrawer(@NonNull Context context, @NonNull String username) {
        context.startActivity(UserProfileActivity.newIntent(context, username, true));
    }

    public void showUserProfileEditor(@NonNull Context context, @NonNull String username) {
        context.startActivity(EditUserProfileActivity.newIntent(context, username));
    }

    public void showCertificate(@NonNull Context context, @NonNull EnrolledCoursesResponse courseData) {
        context.startActivity(CertificateActivity.newIntent(context, courseData));
    }

    public void showCourseDetail(@NonNull Context context, @NonNull CourseDetail courseDetail) {
        context.startActivity(CourseDetailActivity.newIntent(context, courseDetail));
    }

    public void showFindCourses(@NonNull Context context) {
        if (!config.getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            throw new RuntimeException("Course discovery is not enabled");
        }
        final Intent findCoursesIntent;
        if (config.getCourseDiscoveryConfig().isWebviewCourseDiscoveryEnabled()) {
            findCoursesIntent = new Intent(context, WebViewFindCoursesActivity.class);
        } else {
            findCoursesIntent = NativeFindCoursesActivity.newIntent(context);
        }
        //Add this flag as multiple activities need to be created
        findCoursesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(findCoursesIntent);
    }

    public void showExploreSubjects(@NonNull Context context) {
        final Intent findCoursesIntent;
        if (config.getCourseDiscoveryConfig().isWebviewCourseDiscoveryEnabled()) {
            findCoursesIntent = new Intent(context, WebViewExploreSubjectsActivity.class);
        } else {
            throw new RuntimeException("'Explore Subjects' is not implemented for native course discovery");
        }
        //Add this flag as multiple activities need to be created
        findCoursesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(findCoursesIntent);
    }

    public void showWebViewActivity(@NonNull Activity activity, @NonNull String url, @Nullable String title) {
        activity.startActivity(WebViewActivity.newIntent(activity, url, title));
    }

    public void showCourseDatesActivity(@NonNull Activity activity, @NonNull EnrolledCoursesResponse model) {
        activity.startActivity(CourseDatesActivity.newIntent(activity, model));
    }

    public void showWhatsNewActivity(@NonNull Activity activity) {
        activity.startActivity(WhatsNewActivity.newIntent(activity));
    }

    public void showAccountActivity(@NonNull Activity activity) {
        activity.startActivity(AccountActivity.newIntent(activity));
    }

    /**
     * Open an email client for user to write feedback
     *
     * @param activity Activity context
     * @param subject  Subject of email
     */
    public void showFeedbackScreen(@NonNull FragmentActivity activity, @NonNull String subject) {
        final String NEW_LINE = "\n";
        final String to = config.getFeedbackEmailAddress();
        StringBuilder body = new StringBuilder();
        body.append(String.format("%s %s", activity.getString(R.string.android_os_version), android.os.Build.VERSION.RELEASE))
                .append(NEW_LINE)
                .append(String.format("%s %s", activity.getString(R.string.app_version), BuildConfig.VERSION_NAME))
                .append(NEW_LINE)
                .append(String.format("%s %s", activity.getString(R.string.android_device_model), Build.MODEL))
                .append(NEW_LINE).append(NEW_LINE)
                .append(activity.getString(R.string.insert_feedback));
        EmailUtil.openEmailClient(activity, to, subject, body.toString(), config);
    }
}
