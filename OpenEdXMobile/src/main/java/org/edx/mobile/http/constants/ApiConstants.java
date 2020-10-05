package org.edx.mobile.http.constants;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ApiConstants {
    public static final String API_VERSION = "apiVersion";
    public static final String COURSE_ID = "courseId";
    public static final String USER_NAME = "username";
    public static final String GROUP_ID = "groupId";
    public static final String ORG_CODE = "org";

    public static final String URL_MY_USER_INFO ="/api/mobile/v0.5/my_user_info";

    public static final String URL_ACCESS_TOKEN = "/oauth2/access_token/";

    public static final String URL_PASSWORD_RESET = "/password_reset/";

    public static final String URL_EXCHANGE_ACCESS_TOKEN = "/oauth2/exchange_access_token/{" + GROUP_ID + "}/";

    public static final String URL_REVOKE_TOKEN = "/oauth2/revoke_token/";

    public static final String URL_LOGIN = "/oauth2/login/";

    public static final String URL_LAST_ACCESS_FOR_COURSE ="/api/mobile/v0.5/users/{username}/course_status_info/{courseId}";

    public static final String URL_REGISTRATION = "/user_api/{" + API_VERSION + "}/account/registration/";

    public static final String URL_VALIDATE_REGISTRATION_FIELDS = "/api/user/v1/validation/registration";

    public static final String URL_ENROLLMENT = "/api/enrollment/v1/enrollment";

    public static final String URL_COURSE_OUTLINE = "/api/courses/v1/blocks/?course_id={courseId}&username={username}&depth=all&requested_fields={requested_fields}&student_view_data={student_view_data}&block_counts={block_counts}&nav_depth=3";

    public static final String TOKEN_TYPE_ACCESS = "access_token";

    public static final String TOKEN_TYPE_REFRESH = "refresh_token";

    public static final String VALIDATION_DECISIONS = "validation_decisions";

    public static final String COURSE_KEY = "course_key";

    @StringDef({TOKEN_TYPE_ACCESS, TOKEN_TYPE_REFRESH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TokenType {}

    public static final int STANDARD_PAGE_SIZE = 20;

    public static final String PARAM_PAGE_SIZE = "page_size=" + STANDARD_PAGE_SIZE;

    @NonNull
    public static String getOAuthGroupIdForAuthBackend(@NonNull LoginPrefs.AuthBackend authBackend) {
        switch (authBackend) {
            case FACEBOOK: {
                return PrefManager.Value.BACKEND_FACEBOOK;
            }
            case GOOGLE: {
                return PrefManager.Value.BACKEND_GOOGLE;
            }
            case MICROSOFT: {
                return PrefManager.Value.BACKEND_MICROSOFT;
            }
            default: {
                throw new IllegalArgumentException(authBackend.name());
            }
        }
    }
}
