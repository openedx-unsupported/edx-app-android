package org.edx.mobile.social.google;

import android.app.Activity;
import android.content.Context;

import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;

import java.util.List;

/**
 * To make an consistent access interface to google as facebook used.
 *
 * Created by hanning on 3/12/15.
 */
public class GoogleProvider implements SocialProvider{

    private GoogleOauth2 google;

    public GoogleProvider(GoogleOauth2 google){
        this.google = google;
    }

    @Override
    public boolean isLoggedIn() {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void login(Context context, Callback<Void> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void getUser(Context context, Callback<SocialMember> callback) {
       callback.onSuccess(new SocialMember(-1, google.getEmail()));
    }

    @Override
    public void getMyGroups(Context context, Callback<List<SocialGroup>> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void createNewGroup(Context context, String name, String description, String admin, Callback<Long> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void getGroupMembers(Context context, SocialGroup group, Callback<List<SocialMember>> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public Object shareCourse(Activity activity, CourseEntry course) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public Object shareCertificate(Activity activity, CourseEntry course) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public Object shareVideo(Activity activity, String shareTitle, String shareURL) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void getMyFriends(Context context, Callback<List<SocialMember>> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void inviteFriendsListToGroup(Context context, long groupId, List<SocialMember> memberList, Callback<Void> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public SocialMember getUserProfile() {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void launchUserProfile(Context context, String profileId) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }
}
