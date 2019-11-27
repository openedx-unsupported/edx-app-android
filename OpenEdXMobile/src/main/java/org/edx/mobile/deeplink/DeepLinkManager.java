package org.edx.mobile.deeplink;

import android.app.Activity;
import androidx.annotation.NonNull;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.Router;

/**
 * Utility class to handle the navigation within the app through deep links.
 */
public class DeepLinkManager {
    protected static final Logger logger = new Logger(DeepLinkManager.class);

    public static void onDeepLinkReceived(@NonNull Activity activity, @NonNull final DeepLink deepLink) {
        logger.debug("DeepLinkManager received DeepLink with data:\n" + deepLink.toString());
        // Pass the Config class instance as parameter while manually creating the Router object instated of
        // using injection technique. Otherwise Router unable to initiate the config object in it.
        final Router router = new Router(new Config(activity));
        final String username = new LoginPrefs(activity).getUsername();
        @ScreenDef final String screenName = deepLink.getScreenName();
        if (username == null) {
            switch (screenName) {
                case Screen.COURSE_DISCOVERY:
                case Screen.PROGRAM_DISCOVERY:
                case Screen.DEGREE_DISCOVERY:
                    router.showFindCourses(activity, screenName, deepLink.getPathId());
                    break;
                default:
                    activity.startActivity(router.getLogInIntent(deepLink));
                    break;
            }
            return;
        }

        switch (screenName) {
            case Screen.COURSE_DASHBOARD:
            case Screen.COURSE_VIDEOS:
            case Screen.COURSE_DISCUSSION:
            case Screen.COURSE_DATES:
            case Screen.COURSE_HANDOUT:
            case Screen.COURSE_ANNOUNCEMENT:
            case Screen.DISCUSSION_POST:
            case Screen.DISCUSSION_TOPIC: {
                router.showCourseDashboardTabs(activity, null, deepLink.getCourseId(),
                        deepLink.getTopicID(), deepLink.getThreadID(), false, screenName);
                break;
            }
            case Screen.PROGRAM:
            case Screen.COURSE_DISCOVERY:
            case Screen.PROGRAM_DISCOVERY:
            case Screen.DEGREE_DISCOVERY: {
                router.showMainDashboard(activity, screenName, deepLink.getPathId());
                break;
            }
            case Screen.PROFILE: {
                router.showUserProfile(activity, username);
                break;
            }
            case Screen.SETTINGS:
            case Screen.ACCOUNT: {
                router.showAccountActivity(activity, screenName);
                break;
            }
            default: {
                logger.error(new Exception(String.format("Invalid screen name passed for the deeplink %s", screenName)), true);
                break;
            }
        }
    }
}
