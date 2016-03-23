package org.edx.mobile.social.google;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.edx.mobile.http.HttpManager;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.task.Task;


public class GoogleProvider implements SocialProvider {

    private GoogleOauth2 google;

    public GoogleProvider(GoogleOauth2 google) {
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
    public void getUser(Callback<SocialMember> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void getUserInfo(Context context,
                            SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken,
                            final SocialLoginDelegate.SocialUserInfoCallback userInfoCallback) {
        new GoogleUserInfoTask(context, userInfoCallback, accessToken).execute();

    }

    private class GoogleUserInfoTask extends Task<String> {
        private SocialLoginDelegate.SocialUserInfoCallback userInfoCallback;
        private String accessToken;

        public GoogleUserInfoTask(Context activity, SocialLoginDelegate.SocialUserInfoCallback userInfoCallback, String accessToken) {
            super(activity);
            this.userInfoCallback = userInfoCallback;
            this.accessToken = accessToken;
        }

        @Override
        public void onSuccess(String result) {
            userInfoCallback.setSocialUserInfo(((GoogleOauth2) google).getEmail(), result);
        }

        @Override
        public String call() throws Exception {
            //try to wait a while .
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TODO - current code use GoogleAuthUtil for google login,
            //It is hard to synchronize with GoogleApiClient.Builder
            //also the way to handle the session for both google and facebook need
            //the code refactoring.
            Bundle p = new Bundle();
            String url = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken;
            String json = new HttpManager(getContext()).get(url, p).body;
            logger.debug(json);
            Gson gson = new GsonBuilder().create();
            GoogleUserProfile userProfile = gson.fromJson(json, GoogleUserProfile.class);
            String name = userProfile.name;
            if (TextUtils.isEmpty(name)) {
                if (!TextUtils.isEmpty(userProfile.given_name)) {
                    name = userProfile.given_name + " ";
                }
                if (!TextUtils.isEmpty(userProfile.family_name)) {
                    name += userProfile.given_name;
                }
            }
            return name;
        }
    }

}
