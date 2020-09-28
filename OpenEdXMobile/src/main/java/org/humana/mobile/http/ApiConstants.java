package org.humana.mobile.http;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.module.prefs.PrefManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ApiConstants {
    public static final String COURSE_ID = "courseId";
    public static final String USER_NAME = "username";
    public static final String GROUP_ID = "groupId";

    public static final String URL_MY_USER_INFO ="/api/mobile/v0.5/my_user_info";

    public static final String URL_COURSE_ENROLLMENTS =
        "/api/mobile/v0.5/users/{username}/course_enrollments/?format=json?&_v={v}";

    public static final String URL_VIDEO_OUTLINE =
        "/api/mobile/v0.5/video_outlines/courses/{courseId}";

    public static final String URL_ACCESS_TOKEN = "/oauth2/access_token/";

    public static final String URL_PASSWORD_RESET = "/password_reset/";

    public static final String URL_EXCHANGE_ACCESS_TOKEN = "/oauth2/exchange_access_token/{" + GROUP_ID + "}/";

    public static final String URL_REVOKE_TOKEN = "/oauth2/revoke_token/";

    public static final String URL_LAST_ACCESS_FOR_COURSE ="/api/mobile/v0.5/users/{username}/course_status_info/{courseId}";

    public static final String URL_REGISTRATION = "/user_api/v1/account/registration/";

    public static final String URL_REGISTRATION_PART_ONE = "/custom_register/create_user/";

    public static final String URL_REGISTRATION_PART_TWO = "/discourse/updateUserInfoInRegister/";

    public static final String URL_ENROLLMENT = "/api/enrollment/v1/enrollment";

    public static final String URL_COURSE_OUTLINE = "/api/courses/v1/blocks/?course_id={courseId}&username={username}&depth=all&requested_fields={requested_fields}&student_view_data={student_view_data}&block_counts={block_counts}&nav_depth=3";

    public static final String TOKEN_TYPE_ACCESS = "access_token";

    public static final String TOKEN_TYPE_REFRESH = "refresh_token";

    //changes for OTP:MX ::Arjun
    public static final String URL_MX_GENERATE_OTP = "/api/otp/otp_api/";
    public static final String URL_MX_VERIFY_OTP = "api/otp/otp_api/verify/";
    public static final String URL_MX_REGISTER_ME = "/user_api/v1/account/registration/";
    public static final String URL_MX_UPDATE_PROFILE = "/api/otp/user_account/update/";

    //Analytic api url
    public static final String URL_TA_ANALYTIC = "api/teacherappAnalytics/analytics/add/";

    //FireBase token update api url
    public static final String URL_TA_FIREBASE_TOKEN_UPDATE = "/api/teacherappAnalytics/android_token/";

    //Reset pass url
    public static final String URL_MX_RESETPSWD = "/api/otp/user_account/password/update";

    //submit feedback url
    public static final String URL_MX_SUBMIT_FEEDBACK = "/api/otp/feedback/create";

    //forgot password urls ::start
    public static final String URL_MX_MOBILE_NUMBER_VERIFICATION = "/api/otp/reset_password/send_otp";

    public static final String URL_MX_VERIFY_OTP_FOR_FORGOTED_PASSWORD= "/api/otp/reset_password/check_otp";

    public static final String URL_MX_RESET_FORGOTED_PASSWORD = "/api/otp/reset_password/reset_password";
    //forgot password urls ::end

    //Reset mobile number APIs::start
    public static final String URL_MX_USER_RESET_SEND_OTP = "/api/otp/update_username/send_otp";
    public static final String URL_MX_VERIFY_OTP_AND_RESET_USERNAME = "/api/otp/update_username/update_username";

    //Reset mobile number APIs::end

    //Get user district and block API
    public static final String URL_MX_GET_USER_ADDRESS = "/api/otp/serve-state-data/";

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
            default: {
                throw new IllegalArgumentException(authBackend.name());
            }
        }
    }
    //Analytic batch for seperate server
    public static final String URL_ANALYTIC_BATCH = "analytics/add-list/";

    //TinCan batch update
    public static final String URL_TINCAN_ANALYTIC_BATCH = "analytics/add-tincan-data/";

    //Tincan resume payload update
    public static final String URL_TINCAN_RESUME_BATCH = "analytics/save_coursestate/";

    public static final String URL_GET_COURSES_RESPONSE = "/user-dashboard/get_courses_progress/";
    public static final String URL_POST_COURSES_RATING_RESPONSE = "/api/get-courses-insites/";
}
