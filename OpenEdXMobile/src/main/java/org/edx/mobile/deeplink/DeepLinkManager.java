package org.edx.mobile.deeplink;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.view.Router;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class to handle the navigation within the app through deep links.
 */
public class DeepLinkManager {
    protected static final Logger logger = new Logger(DeepLinkManager.class);
    public static final String KEY_CLICKED_BRANCH_LINK = "+clicked_branch_link";
    private static final String KEY_SCREEN_NAME = "screen_name";
    private static final String KEY_COURSE_ID = "course_id";

    public static void parseAndReact(@NonNull Activity activity, @NonNull JSONObject paramsJson) throws JSONException {
        logger.debug("DeepLink received. JSON Details:\n" + paramsJson);
        final Router router = new Router();
        final boolean isLoggedIn = new LoginPrefs(activity).getUsername() != null;
        if (!isLoggedIn) {
            activity.startActivity(router.getLogInIntent());
        } else {
            @ScreenDef final String screenName = paramsJson.getString(KEY_SCREEN_NAME);
            switch (screenName) {
                case Screen.COURSE_DASHBOARD:
                case Screen.COURSE_VIDEOS: {
                    final String courseId = paramsJson.getString(KEY_COURSE_ID);
                    router.showCourseDashboardTabs(activity, null, courseId, false, screenName);
                    break;
                }
            }
        }
    }
}
