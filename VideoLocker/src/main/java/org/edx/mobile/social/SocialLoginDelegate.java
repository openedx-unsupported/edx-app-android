package org.edx.mobile.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.social.google.GoogleOauth2;

/**
 * Code refactored from Login Activity, for the logic of login to social site are the same
 * for both login and registration.
 *
 * Created by hanning on 3/11/15.
 */
public class SocialLoginDelegate {

    protected final Logger logger = new Logger(getClass().getName());

    private Activity activity;
    private SocialLoginCallback callback;
    private SocialUserInfoCallback userInfoCallback;
    private ISocial google, facebook;

    private ISocial.Callback googleCallback = new ISocial.Callback() {
        @Override
        public void onLogin(String accessToken) {
            logger.debug("Google logged in; token= " + accessToken);
            callback.onSocialLoginSuccess(accessToken, PrefManager.Value.BACKEND_GOOGLE);
        }

    };

    private ISocial.Callback facebookCallback = new ISocial.Callback() {

        @Override
        public void onLogin(String accessToken) {
            logger.debug("Facebook logged in; token= " + accessToken);
            callback.onSocialLoginSuccess(accessToken, PrefManager.Value.BACKEND_FACEBOOK);
        }
    };

    public SocialLoginDelegate(Activity activity, Bundle savedInstanceState, SocialLoginCallback callback){

        this.activity = activity;
        this.callback = callback;

        google = SocialFactory.getInstance(activity, SocialFactory.TYPE_GOOGLE);
        google.setCallback(googleCallback);

        facebook = SocialFactory.getInstance(activity, SocialFactory.TYPE_FACEBOOK);
        facebook.setCallback(facebookCallback);

        google.onActivityCreated(activity, savedInstanceState);
        facebook.onActivityCreated(activity, savedInstanceState);
    }

    public void onActivityDestroyed(){
        google.onActivityDestroyed(activity);
        facebook.onActivityDestroyed(activity);
    }

    public void onActivitySaveInstanceState(Bundle outState){
        google.onActivitySaveInstanceState(activity, outState);
        facebook.onActivitySaveInstanceState(activity, outState);
    }

    public void onActivityStarted(){
        google.onActivityStarted(activity);
        facebook.onActivityStarted(activity);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        google.onActivityResult(requestCode, resultCode, data);
        facebook.onActivityResult(requestCode, resultCode, data);
    }

    public void onActivityStopped() {
        google.onActivityStopped(activity);
        facebook.onActivityStopped(activity);
    }

    public void socialLogin(int socialType){
        if ( socialType == SocialFactory.TYPE_FACEBOOK )
           facebook.login();
        else if ( socialType == SocialFactory.TYPE_GOOGLE )
            google.login();
    }

    public void socialLogout(int socialType){
        if ( socialType == SocialFactory.TYPE_FACEBOOK )
            facebook.logout();
        else if ( socialType == SocialFactory.TYPE_GOOGLE )
            google.logout();
    }

    public void getUserInfo(int socialType, final SocialUserInfoCallback userInfoCallback){

        if ( socialType == SocialFactory.TYPE_FACEBOOK ) {
            SocialProvider socialProvider = new FacebookProvider();
            socialProvider.getUser(activity, new SocialProvider.Callback<SocialMember>() {
                @Override
                public void onSuccess(SocialMember response) {
                    userInfoCallback.setSocialUserInfo(response.email, response.fullName);
                }

                @Override
                public void onError(SocialProvider.SocialError err) {
                    //TODO - should we pass error to UI?
                }
            });
        } else if ( socialType == SocialFactory.TYPE_GOOGLE ) {
            userInfoCallback.setSocialUserInfo(((GoogleOauth2)google).getEmail(), null);
        }

    }


    public interface SocialLoginCallback{
        void onSocialLoginSuccess(String accessToken, String backend);
    }

    public interface SocialUserInfoCallback{
        void setSocialUserInfo(String email, String name);
    }

}
