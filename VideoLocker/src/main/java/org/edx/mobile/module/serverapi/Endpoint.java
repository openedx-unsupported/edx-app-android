package org.edx.mobile.module.serverapi;

import org.edx.mobile.util.Config;

/**
 * Created by rohan on 2/7/15.
 */
class Endpoint {
    private static String getHost() {
        return Config.getInstance().getApiHostURL();
    }

    public static String resetPassword() {
        return getHost() + "/password_reset/";
    }

    public static String loginAuth() {
        return getHost() + "/oauth2/access_token/";
    }

    public static String profile() {
        return getHost() + "/api/mobile/v0.5/my_user_info";
    }

    public static String courseHierarchy(String courseId) {
        return getHost() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
    }

    public static String enrollments(String username) {
        return getHost() + "/api/mobile/v0.5/users/" + username + "/course_enrollments/";
    }

    public static String videosByCourseId(String courseId) {
        return getHost() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
    }

    public static String loginWebLink() {
        return getHost() + "/login";
    }

    public static String loginBySocial(String backend) {
        return getHost() + "/login_oauth_token/" + backend + "/";
    }

    public static String syncLastAccessedSubSection(String username, String courseId) {
        return getHost() + "/api/mobile/v0.5/users/" + username + "/course_status_info/" + courseId;
    }

    public static String createAccount() {
        return getHost() + "/create_account";
    }

    public static String registration() {
        return getHost() + "/user_api/v1/account/registration/";
    }

    public static String enroll() {
        return getHost() + "/api/enrollment/v1/enrollment";
    }

    public static String inviteFriendsToGroup(long groupId) {
        return getHost() + "/api/mobile/v0.5/social/facebook/groups/" + Long.toString(groupId) + "/member/";
    }

    public static String createGroup() {
        //String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/groups/create/";
        return getHost() + "/api/mobile/v0.5/social/facebook/groups/";
    }

    public static String friendsCourses() {
        return getHost() + "/api/mobile/v0.5/social/facebook/courses/friends";
    }

    public static String friendsInCourses(String courseId) {
        return getHost() + "/api/mobile/v0.5/social/facebook/friends/course/" + courseId;
    }

    public static String userCourseShareConsent() {
        return getHost() + "/api/mobile/v0.5/settings/preferences/";
    }

    public static String groupMembers(String groupId) {
        return getHost() + "/api/mobile/v0.5/social/facebook/groups/" + groupId + "/members";
    }
}
