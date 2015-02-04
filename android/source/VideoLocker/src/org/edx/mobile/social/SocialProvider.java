package org.edx.mobile.social;

import android.app.Activity;
import android.content.Context;

import org.edx.mobile.model.api.CourseEntry;

import java.util.List;

public interface SocialProvider {

    /**
     * Callback interface for all social network calls
     */
    public interface Callback<T> {
        public void onSuccess(T response);
        public void onError(SocialError err);
    }

    public class SocialError {
        public final String error;
        public final Exception ex;

        public SocialError(String error, Exception ex) {
            this.error = error;
            this.ex = ex;
        }
    }

    /**
     * determines if the user is logged in
     */
    public boolean isLoggedIn();

    /**
     * Begins the login flow for the user. This call is asynchronous
     */
    public void login(Context context, Callback<Void> callback);

    /**
     * Gets the user object for the current logged in user
     */
    public void getUser(Context context, Callback<SocialMember> callback);

    /**
     * Gets the list of registered groups this user is a member of
     */
    public void getMyGroups(Context context, Callback<List<SocialGroup>> callback);

    /**
     * Creates a new group and makes the current user the admin
     */
    public void createNewGroup(Context context, String name, String description, String admin, final Callback<Long> callback);

    /**
     * Get a list of all members in a group
     */
    public void getGroupMembers(Context context, SocialGroup group, Callback<List<SocialMember>> callback);

    ///////////////
    //SHARING
    ///////////////

    /**
     * (Semi)publicly share a course on your social profile page
     */
    public Object shareCourse(Activity activity, CourseEntry course);

    /**
     * (Semi)publicly share a course certificate you have achieved on your social profile page
     */
    public Object shareCertificate(Activity activity, CourseEntry course);

    /**
     * Share video content to the Social feed
     */
    public Object shareVideo(Activity activity, String shareTitle, String shareURL);

    /**
     * Gets the list of Facebook APP friends for the user
     */
    public void getMyFriends(Context context, Callback<List<SocialMember>> callback);

    /**
     * Invites the list of social members to a group
     */
    public void inviteFriendsListToGroup(Context context, long groupId, List<SocialMember> memberList, Callback<Void> callback);

    /**
     * Return a reference to the cached user profile
     */
    public SocialMember getUserProfile();

    /**
     * Launch an intent to view the user profile
     */
    public void launchUserProfile(Context context, String profileId);

}
