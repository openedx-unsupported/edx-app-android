package org.edx.mobile.social.google;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.edx.mobile.http.callback.ErrorHandlingOkCallback;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;

import okhttp3.Request;
import roboguice.RoboGuice;

public class GoogleProvider implements SocialProvider {
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s";

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
        final OkHttpClientProvider okHttpClientProvider = RoboGuice.getInjector(context).getInstance(OkHttpClientProvider.class);
        okHttpClientProvider.get().newCall(new Request.Builder()
                .url(String.format(USER_INFO_URL, accessToken))
                .get()
                .build())
                .enqueue(new ErrorHandlingOkCallback<GoogleUserProfile>(context,
                        GoogleUserProfile.class, null) {
                    @Override
                    protected void onResponse(@NonNull GoogleUserProfile userProfile) {
                        String name = userProfile.name;
                        if (TextUtils.isEmpty(name)) {
                            if (!TextUtils.isEmpty(userProfile.given_name)) {
                                name = userProfile.given_name + " ";
                            }
                            if (!TextUtils.isEmpty(userProfile.family_name)) {
                                if (TextUtils.isEmpty(name)) {
                                    name = userProfile.family_name;
                                } else {
                                    name += userProfile.family_name;
                                }
                            }
                        }
                        userInfoCallback.setSocialUserInfo(google.getEmail(), name);
                    }
                });
    }
}
