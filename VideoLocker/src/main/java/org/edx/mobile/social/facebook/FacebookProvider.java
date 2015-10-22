package org.edx.mobile.social.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.json.FriendListResponse;
import org.edx.mobile.model.json.GroupListResponse;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.task.social.CreateGroupTask;
import org.edx.mobile.task.social.InviteFriendsListToGroupTask;
import org.edx.mobile.module.facebook.FacebookSessionUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.SocialUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class FacebookProvider implements SocialProvider {

    protected final Logger logger = new Logger(getClass().getName());
    private static final String TAG = FacebookProvider.class.getCanonicalName();

    private SocialMember userProfile;

    @Inject
    Config config;

    public static String createFacebookPhotoURI(long userID){

        return "http://graph.facebook.com/" + userID + "/picture";

    }

    private boolean notifyIfNotLoggedIn(Context context, Callback callback) {
        if (!isLoggedIn()) {
            callback.onError(new SocialError(context.getString(R.string.error_no_fb), null));
            return true;
        }
        return false;
    }

    /**
     * returns true if there was an error. The callback will be notified
     */
    private boolean notifyIfError(Response response, Callback callback) {
        if (response.getError() != null) {
            FacebookRequestError error = response.getError();
            callback.onError(new SocialError(error.getErrorMessage(), error.getException()));
            return true;
        }
        return false;
    }

    @Override
    public boolean isLoggedIn() {
        Session session = Session.getActiveSession();
        return session != null && session.isOpened();
    }

    @Override
    public void login(Context context, Callback<Void> callback) {

        userProfile = null;

        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    public void getUserInfo(Context context,SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken,
                            final SocialLoginDelegate.SocialUserInfoCallback userInfoCallback){
        getUser(context, new SocialProvider.Callback<SocialMember>() {
            @Override
            public void onSuccess(SocialMember response) {
                userInfoCallback.setSocialUserInfo(response.getEmail(), response.getFullName());
            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                logger.warn(err.toString());
            }
        });
    }


    @Override
    public void getUser(Context context, final Callback<SocialMember> callback) {

        if (notifyIfNotLoggedIn(context, callback)) {
            return;
        }

        //If the profile for the current user has already been fetched use the cached result.
        if (userProfile != null) {
            callback.onSuccess(userProfile);
            return;
        }

        Session session = Session.getActiveSession();
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (!notifyIfError(response, callback)) {
                    logger.debug(user.getUsername() + ":" + user.getName() + ":" + user.getFirstName()
                            + ":" + user.getLastName() + ":" + user.getId());
                    logger.debug( user.getProperty("email") + "");
                    SocialMember member = new SocialMember(Long.parseLong(user.getId()), user.getName());
                    member.setEmail(user.getProperty("email") + "");
                    callback.onSuccess( member );

                }
            }
        });
        Bundle params = request.getParameters();
        params.putString("fields", "email,id,name");
        request.setParameters(params);
        request.executeAsync();
    }

    @Override
    public void getMyGroups(Context context, final Callback<List<SocialGroup>> callback) {

        Request.Callback getMyGroupsCallback = new Request.Callback(){

            public void onCompleted(Response response) {

                if (notifyIfError(response, callback)) {
                    return;
                }

                String json;
                GraphObject graph = response.getGraphObject();
                try {
                    json = graph.getInnerJSONObject().getJSONArray("data").toString();
                } catch (Exception e) {
                    callback.onError(new SocialError("There was an unknown error", e));
                    return;
                }

                Gson gson = new Gson();
                GroupListResponse[] groupItems = gson.fromJson(json, GroupListResponse[].class);

                List<SocialGroup> socialGroups = new ArrayList<SocialGroup>(groupItems.length);
                for (GroupListResponse group : groupItems) {
                    SocialGroup socialGroup = new SocialGroup(group.getId(), group.getName(), group.getDescription(), group.getUnread());
                    socialGroups.add(socialGroup);
                }
                callback.onSuccess(socialGroups);
            }

        };
        new Request(Session.getActiveSession(), "/me/groups", null, HttpMethod.GET, getMyGroupsCallback).executeAsync();

    }

    @Override
    public void inviteFriendsListToGroup(Context context, long groupID, List<SocialMember> friendList, final Callback<Void> callback) {

        String token = FacebookSessionUtil.getAccessToken();

        Long[] friendIDs = new Long[friendList.size()];
        for (int i = 0; i < friendList.size(); i++){
            friendIDs[i] = friendList.get(i).getId();
        }

        InviteFriendsListToGroupTask inviteFriendsListToGroupTask = new InviteFriendsListToGroupTask(context, friendIDs, groupID, token) {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onException(Exception ex) {
                callback.onError(new SocialError("There was an unknown error", ex));
            }
        };
        inviteFriendsListToGroupTask.execute();

    }

    @Override
    public void createNewGroup(Context context, String name, String description, String admin, final Callback<Long> callback) {

        CreateGroupTask createGroupTask = new CreateGroupTask(context, name, description, admin, false) {

            @Override
            public void onSuccess(Long groupID) {
                callback.onSuccess(groupID);
            }

            @Override
            public void onException(Exception ex) {
                callback.onError(new SocialError("There was an unknown error", ex));
            }

        };
        createGroupTask.execute();

    }

    @Override
    public void getMyFriends(final Context context, final Callback<List<SocialMember>> callback) {

        if (notifyIfNotLoggedIn(context, callback)) {
            return;
        }

        Request.Callback getMyFriendsCallback = new Request.Callback() {

            public void onCompleted(Response response) {

                if (notifyIfError(response, callback)) {
                    return;
                }

                String json;
                GraphObject graph = response.getGraphObject();
                try {
                    json = graph.getInnerJSONObject().getJSONArray("data").toString();
                } catch (Exception e) {
                    callback.onError(new SocialError(context.getString(R.string.error_unknown_title), e));
                    return;
                }

                Gson gson = new Gson();
                FriendListResponse[] appMembers = gson.fromJson(json, FriendListResponse[].class);

                List<SocialMember> membersArrayList = new ArrayList<SocialMember>(appMembers.length);
                for (FriendListResponse member : appMembers) {

                    String first = member.getName().substring(0, member.getName().indexOf(' '));
                    String last = member.getName().substring(member.getName().indexOf(' ')+1);
                    String photo = FacebookProvider.createFacebookPhotoURI(member.getId());
                    SocialMember socialMember = new SocialMember(member.getId(), first, last, photo);
                    membersArrayList.add(socialMember);
                }
                callback.onSuccess(membersArrayList);

            }

        };
        new Request(Session.getActiveSession(), "/me/friends", null, HttpMethod.GET, getMyFriendsCallback).executeAsync();

    }

    @Override
    public void launchUserProfile(Context context, String profileId) {

        PackageManager pm = context.getPackageManager();
        Uri uri;

        uri = Uri.parse("fb://profile/" + profileId);
        if (!SocialUtils.isUriAvailable(pm, uri)){
            uri = Uri.parse("https://m.facebook.com/" + profileId);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        context.startActivity(intent);


    }

    @Override
    public SocialMember getUserProfile() {
        return userProfile;
    }

    @Override
    public void getGroupMembers(final Context context, SocialGroup group, final Callback<List<SocialMember>> callback) {

        if (notifyIfNotLoggedIn(context, callback)) {
            return;
        }

        Request.Callback getGroupMembersCallback = new Request.Callback() {
            @Override
            public void onCompleted(Response response) {

                if (notifyIfError(response, callback)) {
                    return;
                }

                String json;
                GraphObject graph = response.getGraphObject();
                try {
                    json = graph.getInnerJSONObject().getJSONArray("data").toString();
                } catch (Exception e) {
                    callback.onError(new SocialError(context.getString(R.string.error_unknown_title), e));
                    return;
                }

                Gson gson = new Gson();

                FriendListResponse[] appMembers = gson.fromJson(json, FriendListResponse[].class);

                List<SocialMember> membersArrayList = new ArrayList<SocialMember>(appMembers.length);
                for (FriendListResponse member : appMembers) {

                    String first = member.getName().substring(0, member.getName().indexOf(' '));
                    String last = member.getName().substring(member.getName().indexOf(' ') + 1);
                    String pictureURL = createFacebookPhotoURI(member.getId());

                    SocialMember socialMember = new SocialMember(member.getId(), first, last, pictureURL);
                    membersArrayList.add(socialMember);

                }
                callback.onSuccess(membersArrayList);

            }
        };
        String uri = "/"+ group.getId() + "/members";
        new Request(Session.getActiveSession(), uri, null, HttpMethod.GET, getGroupMembersCallback).executeAsync();

    }

    public Object shareApplication(Activity activity) {

        if (!FacebookDialog.canPresentShareDialog(activity)) {
            return null;
        }

        FacebookDialog.ShareDialogBuilder dialogBuilder = new FacebookDialog.ShareDialogBuilder(activity);
        String name = ResourceUtil.getFormattedString(activity.getResources(), R.string.share_application_title, "platform_name", config.getPlatformName()).toString();
        dialogBuilder.setLink(activity.getString(R.string.share_application_link_url));
        dialogBuilder.setPicture(activity.getString(R.string.share_application_picture_url));
        dialogBuilder.setName(name);
        dialogBuilder.setDescription(activity.getString(R.string.share_application_description));

        return dialogBuilder.build();

    }

    @Override
    public Object shareCourse(Activity activity, CourseEntry course) {

        if (!FacebookDialog.canPresentOpenGraphActionDialog(activity)) {
            return null;
        }

        String shareID = activity.getResources().getString(R.string.facebook_app_share_root);
        String courseID = activity.getResources().getString(R.string.facebook_app_course_id);
        String typeID = activity.getResources().getString(R.string.facebook_app_share_type);

        String courseShareObject = String.format("%s:%s", shareID, courseID);
        String courseShareType = String.format("%s:%s", shareID, typeID);

        OpenGraphObject shareObject = OpenGraphObject.Factory.createForPost(courseShareObject);

        String imageURL = course.getCourse_image(config);

        shareObject.setImageUrls(Arrays.asList(imageURL));
        shareObject.setTitle(course.getName());
        shareObject.setUrl(course.getCourse_url());

        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty(courseID, shareObject);
        action.setType(courseShareType);

        FacebookDialog.OpenGraphActionDialogBuilder dialogBuilder = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, courseID);
        return dialogBuilder.build();

    }

    @Override
    public Object shareCertificate(Activity activity, CourseEntry course) {

        if (!FacebookDialog.canPresentOpenGraphActionDialog(activity)) {
            return null;
        }

        String shareID = activity.getResources().getString(R.string.facebook_app_share_root);
        String certID = activity.getResources().getString(R.string.facebook_app_certificate_id);
        String typeID = activity.getResources().getString(R.string.facebook_app_share_type);

        String certShareObject = String.format("%s:%s",shareID, certID);
        String certShareType = String.format("%s:%s", shareID, typeID);

        OpenGraphObject shareObject = OpenGraphObject.Factory.createForPost(certShareObject);

        String imageURL = course.getCourse_image(config);
        String certURL =  "http://www.edx.org";

        shareObject.setImageUrls(Arrays.asList(imageURL));
        shareObject.setTitle(course.getName());
        shareObject.setUrl(certURL);

        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty(certID, shareObject);
        action.setType(certShareType);

        FacebookDialog.OpenGraphActionDialogBuilder dialogBuilder = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, certID);

        return dialogBuilder.build();

    }

    @Override
    public Object shareVideo(Activity activity, String shareTitle, String shareURL) {

        if (!FacebookDialog.canPresentOpenGraphActionDialog(activity)) {
            return null;
        }

        FacebookDialog.ShareDialogBuilder dialogBuilder = new FacebookDialog.ShareDialogBuilder(activity);
        dialogBuilder.setLink(shareURL);
        dialogBuilder.setName(shareTitle);

        return dialogBuilder.build();

    }

}
