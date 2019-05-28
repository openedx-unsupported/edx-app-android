package org.edx.mobile.http.constants;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ApiConstants {
    public static final String COURSE_ID = "courseId";
    public static final String USER_NAME = "username";
    public static final String GROUP_ID = "groupId";
    public static final String ORG_CODE = "org";

    public static final String URL_MY_USER_INFO ="api/mobile/v0.5/my_user_info";

    public static final String URL_COURSE_ENROLLMENTS =
        "api/mobile/v0.5/users/{username}/course_enrollments/?format=json";

    public static final String URL_VIDEO_OUTLINE =
        "api/mobile/v0.5/video_outlines/courses/{courseId}";

    public static final String URL_ACCESS_TOKEN = "oauth2/access_token/";

    public static final String URL_PASSWORD_RESET = "password_reset/";

    public static final String URL_EXCHANGE_ACCESS_TOKEN = "oauth2/exchange_access_token/{" + GROUP_ID + "}/";

    public static final String URL_REVOKE_TOKEN = "oauth2/revoke_token/";

    public static final String URL_LOGIN = "oauth2/login/";

    public static final String URL_LAST_ACCESS_FOR_COURSE ="api/mobile/v0.5/users/{username}/course_status_info/{courseId}";

    public static final String URL_REGISTRATION = "user_api/v1/account/registration/";

    public static final String URL_ENROLLMENT = "api/enrollment/v1/enrollment";

    public static final String URL_COURSE_OUTLINE = "api/courses/v1/blocks/?course_id={courseId}&username={username}&depth=all&requested_fields={requested_fields}&student_view_data={student_view_data}&block_counts={block_counts}&nav_depth=3";

    //TTA start

    //forgot password urls ::start
    public static final String URL_MX_MOBILE_NUMBER_VERIFICATION = "api/otp/reset_password/send_otp";

    public static final String URL_MX_VERIFY_OTP_FOR_FORGOTED_PASSWORD= "api/otp/reset_password/check_otp";

    public static final String URL_MX_RESET_FORGOTED_PASSWORD = "api/otp/reset_password/reset_password";
    //forgot password urls ::end

    //Reset mobile number APIs::start
    public static final String URL_MX_USER_RESET_SEND_OTP = "api/otp/update_username/send_otp";

    public static final String URL_MX_VERIFY_OTP_AND_RESET_USERNAME = "api/otp/update_username/update_username";
    //Reset mobile number APIs::end

    public static final String URL_MX_GENERATE_OTP = "api/otp/otp_api/";
    public static final String URL_MX_CUSTOM_FIELD_ATTRIBUTES= "api/otp/get-teacher-id-field-info/";
    public static final String URL_MX_VERIFY_OTP = "api/otp/otp_api/verify/";
    public static final String URL_MX_REGISTER_ME = "user_api/v1/account/registration/";
    public static final String URL_MX_UPDATE_PROFILE = "api/otp/user_account/update/";

    public static final String URL_MX_GET_COLLECTION_CONFIG = "api/mx_catalog/v1/config/";

    public static final String URL_MX_GET_CONFIG_MODIFIED_DATE = "api/mx_catalog/v1/modifications/";

    public static final String URL_MX_GET_COLLECTION_ITEMS = "api/mx_catalog/v1/contents/";

    public static final String URL_MX_GET_STATE_AGENDA_COUNT = "api/mx_catalog/v1/stateagenda/";

    public static final String URL_MX_GET_MY_AGENDA_COUNT = "api/mx_catalog/v1/myagenda/";

    public static final String URL_MX_GET_MY_AGENDA_CONTENT = "api/mx_catalog/v1/myagendacontent/";

    public static final String URL_MX_GET_STATE_AGENDA_CONTENT = "api/mx_catalog/v1/stateagendacontent/";

    public static final String URL_MX_SET_BOOKMARK = "api/mx_catalog/v1/bookmark/";

    //Get user district and block API
    public static final String URL_MX_GET_USER_ADDRESS = "api/otp/serve-state-data/";

    public static final String URL_MX_SET_LIKE = "api/mx_catalog/v1/like/";

    public static final String URL_MX_TOTAL_LIKE = "api/mx_catalog/v1/totallike/";

    public static final String URL_MX_IS_LIKE = "api/mx_catalog/v1/islike/";

    public static final String URL_MX_IS_CONTENT_MY_AGENDA = "api/mx_catalog/v1/ismyagenda/";

    public static final String URL_MX_USER_ENROLLMENT_COURSE = "api/mx_enrollment/v1/enrollmentcourse/";

    //Post scorm start request
    public static final String URL_MX_SCORM_START = "api/mx_scorm/scorm_started/";

    public static final String URL_MX_GET_SEARCH_FILTER = "api/mx_catalog/v1/getfilters/";

    public static final String URL_MX_SEARCH = "api/mx_catalog/v3/search/";
    //assistant search
    public static final String URL_MX_ASSISTANT_SEARCH = "api/mx_catalog/v1/gasearch/";

    //submit feedback url
    public static final String URL_MX_SUBMIT_FEEDBACK = "api/otp/feedback/create";

    //Reset pass url
    public static final String URL_MX_RESETPSWD = "api/otp/user_account/password/update";

    //Get all certificates of user
    public static final String URL_MX_GET_MY_CERTIFICATES = "api/mxcertification_api/get_my_certificates/";

    //Get status of course certificate
    public static final String URL_MX_GET_CERTIFICATE_STATUS = "api/mxcertification_api/get_certificate_status/";

    //Get course certificate
    public static final String URL_MX_GET_CERTIFICATE = "api/mxcertification_api/get_certificate/";

    //Post generate certificate request
    public static final String URL_MX_GENERATE_CERTIFICATE = "api/mxcertification_api/generate_certificate/";

    public static final String URL_MX_GET_CONTENT = "api/mx_catalog/v1/content/";

    public static final String URL_MX_GET_SUGGESTED_USERS = "api/mx_recommended_users/v1/getrecommendedusers/";

    public static final String URL_MX_FOLLOW_USER = "api/mx_feed/v1/follow/";

    public static final String URL_MX_CREATE_GET_NOTIFICATIONS = "notification/v1/showcreate/";

    public static final String URL_MX_UPDATE_NOTIFICATIONS = "notification/v1/update/";

    public static final String URL_MX_FIREBASE_TOKEN_UPDATE = "api/teacherappAnalytics/android_token/";

    //Analytic api url
    public static final String URL_TA_ANALYTIC = "api/teacherappAnalytics/analytics/add/";

    //Analytic batch api url
    public static final String URL_TA_ANALYTIC_BATCH = "api/teacherappAnalytics/analytics/add-list/";

    //Analytic batch for seperate server
    public static final String URL_ANALYTIC_BATCH = "/analytics/add-list/";

    //TinCan batch update
    public static final String URL_TINCAN_ANALYTIC_BATCH = "/analytics/add-tincan-data/";

    public static final String URL_MX_GET_FEEDS = "api/mx_feed/v1/feeds/";

    public static final String URL_MX_SET_USER_CONTENT = "api/mx_content_analytics/v1/setusercontent/";

    public static final String URL_MX_GET_MY_CONTENT_STATUS = "api/mx_content_analytics/v1/getmycontentstatus/";

    public static final String URL_MX_GET_USER_CONTENT_STATUS = "api/mx_content_analytics/v1/getcontentstatus/";

    public static final String URL_MX_GET_UNIT_STATUS = "api/mx_content_analytics/v1/getunitstatus/";

    public static final String URL_MX_GET_FOLLOW_STATUS = "api/mx_feed/v1/getfollowstatus/";

    //TTA end

    public static final String TOKEN_TYPE_ACCESS = "access_token";

    public static final String TOKEN_TYPE_REFRESH = "refresh_token";

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
}
