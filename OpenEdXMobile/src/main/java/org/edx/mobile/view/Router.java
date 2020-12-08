package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.app.TaskStackBuilder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.deeplink.DeepLink;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.profiles.UserProfileActivity;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.SecurityUtil;
import org.edx.mobile.util.links.WebViewLink;
import org.edx.mobile.whatsnew.WhatsNewActivity;

@Singleton
public class Router {
    public static final String EXTRA_ANNOUNCEMENTS = "announcements";
    public static final String EXTRA_BUNDLE = "bundle";
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_DETAIL = "course_detail";
    public static final String EXTRA_COURSE_DATA = "course_data";
    public static final String EXTRA_COURSE_UPGRADE_DATA = "course_upgrade_data";
    public static final String EXTRA_COURSE_UNIT = "course_unit";
    public static final String EXTRA_COURSE_COMPONENT_ID = "course_component_id";
    public static final String EXTRA_LAST_ACCESSED_ID = "last_accessed_id";
    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_DISCUSSION_TOPIC = "discussion_topic";
    public static final String EXTRA_DISCUSSION_THREAD = "discussion_thread";
    public static final String EXTRA_DISCUSSION_THREAD_ID = "discussion_thread_id";
    public static final String EXTRA_DISCUSSION_COMMENT = "discussion_comment";
    public static final String EXTRA_DISCUSSION_TOPIC_ID = "discussion_topic_id";
    public static final String EXTRA_IS_VIDEOS_MODE = "videos_mode";
    public static final String EXTRA_IS_ON_COURSE_OUTLINE = "is_on_course_outline";
    public static final String EXTRA_SUBJECT_FILTER = "subject_filter";
    public static final String EXTRA_PATH_ID = "path_id";
    public static final String EXTRA_SCREEN_NAME = "screen_name";
    public static final String EXTRA_SCREEN_SELECTED = "screen_selected";
    public static final String EXTRA_DEEP_LINK = "deep_link";
    public static final String EXTRA_ENROLLMENT_MODE = "enrollment_mode";

    @Inject
    Config config;

    @Inject
    private LoginAPI loginAPI;
    @Inject
    private LoginPrefs loginPrefs;
    @Inject
    private IStorage storage;

    public Router() {
    }

    public Router(Config config) {
        this.config = config;
    }

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
        courseInfoIntent.putExtra(EXTRA_PATH_ID, pathId);
        sourceActivity.startActivity(courseInfoIntent);
    }

    public void showProgramInfo(Activity sourceActivity, String pathId) {
        Intent programInfoIntent = new Intent(sourceActivity, ProgramInfoActivity.class);
        programInfoIntent.putExtra(EXTRA_PATH_ID, pathId);
        sourceActivity.startActivity(programInfoIntent);
    }

    public void showSettings(Activity sourceActivity) {
        Intent settingsIntent = new Intent(sourceActivity, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
    public Intent getLogInIntent(@Nullable DeepLink deepLink) {
        return LoginActivity.newIntent(deepLink);
    }

    @NonNull
    public Intent getLogInIntent() {
        return getLogInIntent(null);
    }

    @NonNull
    public Intent getRegisterIntent() {
        return RegisterActivity.newIntent();
    }

    public void showMainDashboard(@NonNull Activity sourceActivity) {
        showMainDashboard(sourceActivity, null);
    }

    public void showMainDashboard(@NonNull Activity sourceActivity, @Nullable @ScreenDef String screenName) {
        showMainDashboard(sourceActivity, screenName, null);
    }

    public void showMainDashboard(@NonNull Activity sourceActivity, @Nullable @ScreenDef String screenName,
                                  @Nullable String pathId) {
        Intent intent = MainDashboardActivity.newIntent(screenName, pathId);
        if (!TextUtils.isEmpty(screenName)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        sourceActivity.startActivity(intent);
    }

    public void showCourseDashboardTabs(@NonNull Activity activity,
                                        @Nullable EnrolledCoursesResponse model,
                                        boolean announcements) {
        showCourseDashboardTabs(activity, model, null, null, null, announcements, null);
    }

    public void showCourseDashboardTabs(@NonNull Activity activity,
                                        @Nullable EnrolledCoursesResponse model,
                                        @Nullable String courseId,
                                        @Nullable String topicId,
                                        @Nullable String threadId,
                                        boolean announcements,
                                        @Nullable @ScreenDef String screenName) {
        activity.startActivity(CourseTabsDashboardActivity.newIntent(activity, model, courseId,
                topicId, threadId, announcements, screenName));
    }

    public void showCourseUpgradeWebViewActivity(@NonNull Context context,
                                                 @NonNull String webUrl,
                                                 @NonNull EnrolledCoursesResponse courseData,
                                                 @Nullable CourseComponent unit) {
        context.startActivity(
                CourseUpgradeWebViewActivity.newIntent(context, webUrl,
                        context.getResources().getString(R.string.place_order_title), courseData, unit)
        );
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
        // TODO: It's not essential, but we may want additional activities on the back-stack
        TaskStackBuilder.create(context)
                .addNextIntent(courseDetail)
                .startActivities();
    }

    public void showCourseContainerOutline(Activity activity, EnrolledCoursesResponse courseData,
                                           CourseUpgradeResponse courseUpgradeData,
                                           String courseComponentId) {
        Intent courseDetail = CourseOutlineActivity.newIntent(activity, courseData,
                courseUpgradeData, courseComponentId, null, false);
        //TODO - what's the most suitable FLAG?
        // courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivityForResult(courseDetail, -1);
    }

    public void showCourseContainerOutline(Fragment fragment, int requestCode,
                                           EnrolledCoursesResponse courseData,
                                           CourseUpgradeResponse courseUpgradeData,
                                           String courseComponentId,
                                           String lastAccessedId, boolean isVideosMode) {
        Intent courseDetail = CourseOutlineActivity.newIntent(fragment.getActivity(),
                courseData, courseUpgradeData, courseComponentId, lastAccessedId, isVideosMode);
        //TODO - what's the most suitable FLAG?
        // courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(courseDetail, requestCode);
    }

    public void showCourseUnitDetail(Fragment fragment, int requestCode, EnrolledCoursesResponse model,
                                     CourseUpgradeResponse courseUpgradeData,
                                     String courseComponentId, boolean isVideosMode) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, model);
        courseBundle.putParcelable(EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData);
        courseBundle.putSerializable(EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        Intent courseDetail = new Intent(fragment.getActivity(), CourseUnitNavigationActivity.class);
        courseDetail.putExtra(EXTRA_BUNDLE, courseBundle);
        courseDetail.putExtra(EXTRA_IS_VIDEOS_MODE, isVideosMode);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(courseDetail, requestCode);
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

    public void showCourseDiscussionPostsForDiscussionTopic(Activity activity, String topicId,
                                                            String threadId, EnrolledCoursesResponse courseData) {
        showCourseDiscussionPostsForDiscussionTopic(activity, null, topicId, threadId, courseData);
    }

    public void showCourseDiscussionPostsForDiscussionTopic(Activity activity, DiscussionTopic topic,
                                                            EnrolledCoursesResponse courseData) {
        showCourseDiscussionPostsForDiscussionTopic(activity, topic, null, null, courseData);
    }

    public void showCourseDiscussionPostsForDiscussionTopic(Activity activity, DiscussionTopic topic,
                                                            String topicId, String threadId,
                                                            EnrolledCoursesResponse courseData) {
        Intent showDiscussionPostsIntent = new Intent(activity, CourseDiscussionPostsActivity.class);
        showDiscussionPostsIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        if (topic != null) {
            showDiscussionPostsIntent.putExtra(EXTRA_DISCUSSION_TOPIC, topic);
        }
        showDiscussionPostsIntent.putExtra(Router.EXTRA_DISCUSSION_TOPIC_ID, topicId);
        showDiscussionPostsIntent.putExtra(Router.EXTRA_DISCUSSION_THREAD_ID, threadId);
        showDiscussionPostsIntent.putExtra(CourseDiscussionPostsThreadFragment.ARG_DISCUSSION_HAS_TOPIC_NAME, topic != null);
        showDiscussionPostsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(showDiscussionPostsIntent);
    }

    public void showCourseDiscussionResponses(@NonNull Context context, @Nullable String threadId,
                                              @NonNull EnrolledCoursesResponse courseData) {
        showCourseDiscussionResponses(context, null, threadId, courseData);
    }

    public void showCourseDiscussionResponses(@NonNull Context context,
                                              @Nullable DiscussionThread discussionThread,
                                              @NonNull EnrolledCoursesResponse courseData) {
        showCourseDiscussionResponses(context, discussionThread, null, courseData);
    }

    public void showCourseDiscussionResponses(@NonNull Context context,
                                              @Nullable DiscussionThread discussionThread,
                                              @Nullable String threadId,
                                              @NonNull EnrolledCoursesResponse courseData) {
        Intent discussionResponsesIntent = new Intent(context, CourseDiscussionResponsesActivity.class);
        if (discussionThread != null) {
            discussionResponsesIntent.putExtra(EXTRA_DISCUSSION_THREAD, discussionThread);
        } else if (threadId != null) {
            discussionResponsesIntent.putExtra(EXTRA_DISCUSSION_THREAD_ID, threadId);
        }
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
        context.startActivity(UserProfileActivity.newIntent(context, username));
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

    public void showFindCourses(@NonNull Context context, @Nullable String searchQuery) {
        showFindCourses(context, searchQuery, null, null);
    }

    public void showFindCourses(@NonNull Context context, @ScreenDef @Nullable String screenName, @Nullable String pathId) {
        showFindCourses(context, null, screenName, pathId);
    }

    public void showFindCourses(@NonNull Context context, @Nullable String searchQuery,
                                @ScreenDef @Nullable String screenName, @Nullable String pathId) {
        if (!config.getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled()) {
            throw new RuntimeException("Course discovery is not enabled");
        }
        final Intent discoveryIntent = DiscoveryActivity.newIntent(context);
        if (searchQuery != null) {
            discoveryIntent.putExtra(Router.EXTRA_SEARCH_QUERY, searchQuery);
        }
        discoveryIntent.putExtra(EXTRA_SCREEN_NAME, screenName);
        discoveryIntent.putExtra(Router.EXTRA_PATH_ID, pathId);
        //Add this flag as multiple activities need to be created
        discoveryIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(discoveryIntent);
    }

    public void showWhatsNewActivity(@NonNull Activity activity) {
        activity.startActivity(WhatsNewActivity.newIntent(activity));
    }

    public void showAccountActivity(@NonNull Activity activity) {
        activity.startActivity(AccountActivity.newIntent(activity, null));
    }

    public void showAccountActivity(@NonNull Activity activity, @Nullable @ScreenDef String screenName) {
        activity.startActivity(AccountActivity.newIntent(activity, screenName));
    }

    public void showPaymentsInfoActivity(@NonNull Context context, @NonNull EnrolledCoursesResponse courseDate,
                                         @NonNull CourseUpgradeResponse courseUpgrade) {
        context.startActivity(PaymentsInfoActivity.Companion.newIntent(context, courseDate, courseUpgrade));
    }

    public void showSubjectsActivityForResult(@NonNull Fragment fragment, int requestCode) {
        fragment.startActivityForResult(ViewSubjectsActivity.newIntent(fragment.getActivity()), requestCode);
    }

    public void showProgramWebViewActivity(@NonNull Activity activity, final IEdxEnvironment environment,
                                           final String pathId, @NonNull String title) {
        final CharSequence url = ResourceUtil.getFormattedString(
                environment.getConfig().getProgramConfig().getDetailUrlTemplate(),
                WebViewLink.Param.PATH_ID, pathId);
        activity.startActivity(ProgramWebViewActivity.newIntent(activity, url.toString(), title));
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
