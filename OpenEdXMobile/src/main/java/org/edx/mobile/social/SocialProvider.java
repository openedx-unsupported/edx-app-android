package org.edx.mobile.social;

import android.content.Context;

public interface SocialProvider {

    /**
     * Callback interface for all social network calls
     */
    interface Callback<T> {
        void onSuccess(T response);

        void onError(SocialError err);
    }

    class SocialError {
        public final Exception ex;

        public SocialError(Exception ex) {
            this.ex = ex;
        }
    }

    /**
     * determines if the user is logged in
     */
    boolean isLoggedIn();

    /**
     * Begins the login flow for the user. This call is asynchronous
     */
    void login(Context context, Callback<Void> callback);

    /**
     * Gets the user object for the current logged in user
     */
    void getUser(Callback<SocialMember> callback);

    /**
     * Gets the user object for the current logged in user
     */
    void getUserInfo(Context context, SocialSourceType socialType, String accessToken, SocialLoginDelegate.SocialUserInfoCallback userInfoCallback);
}
