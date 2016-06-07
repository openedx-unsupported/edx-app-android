package org.edx.mobile.http;

import android.support.annotation.NonNull;

import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;

public class ApiConstants {

    public static final String URL_MY_USER_INFO ="/api/mobile/v0.5/my_user_info";

    public static final String URL_COURSE_ENROLLMENTS =
        "/api/mobile/v0.5/users/{username}/course_enrollments/?format=json";

    public static final String URL_VIDEO_OUTLINE =
        "/api/mobile/v0.5/video_outlines/courses/{courseId}";

    public static final String URL_ACCESS_TOKEN = "/oauth2/access_token/";

    public static final String URL_PASSWORD_RESET = "/password_rest/";

    public static final String URL_EXCHANGE_ACCESS_TOKEN = "/oauth2/exchange_access_token/{backend}/";

    public static final String URL_LAST_ACCESS_FOR_COURSE ="/api/mobile/v0.5/users/{username}/course_status_info/{courseId}";

    public static final String URL_REGISTRATION = "/user_api/v1/account/registration/";

    public static final String URL_ENROLLMENT = "/api/enrollment/v1/enrollment";

    public static final String URL_COURSE_OUTLINE = "/api/courses/v1/blocks/?course_id={courseId}&username={username}&depth=all&requested_fields={requested_fields}&student_view_data={student_view_data}&block_counts={block_counts}&nav_depth=3";

    public static final String COURSE_ID = "courseId";
    public static final String USER_NAME = "username";
    public static final String GROUP_ID = "groupId";

    public static final int STANDARD_PAGE_SIZE = 20;

    @NonNull
    public static String getOAuthGroupIdForAuthBackend(@NonNull LoginPrefs.AuthBackend authBackend) {
        switch (authBackend) {
            case FACEBOOK: {
                return PrefManager.Value.BACKEND_FACEBOOK;
            }
            case GOOGLE: {
                return PrefManager.Value.BACKEND_GOOGLE;
            }
            default: {
                throw new IllegalArgumentException(authBackend.name());
            }
        }
    }
}
