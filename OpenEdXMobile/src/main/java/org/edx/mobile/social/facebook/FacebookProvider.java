package org.edx.mobile.social.facebook;

import android.content.Context;
import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;

@Singleton
public class FacebookProvider implements SocialProvider {
    protected final Logger logger = new Logger(getClass().getName());

    private SocialMember userProfile;

    private boolean notifyIfNotLoggedIn(Callback callback) {
        if (!isLoggedIn()) {
            callback.onError(new SocialError(null));
            return true;
        }
        return false;
    }

    @Override
    public boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    @Override
    public void login(Context context, Callback<Void> callback) {
        userProfile = null;
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    public void getUserInfo(Context context, SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken,
                            final SocialLoginDelegate.SocialUserInfoCallback userInfoCallback) {
        getUser(new SocialProvider.Callback<SocialMember>() {
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
    public void getUser(final Callback<SocialMember> callback) {
        if (notifyIfNotLoggedIn(callback)) {
            return;
        }

        //If the profile for the current user has already been fetched use the cached result.
        if (userProfile != null) {
            callback.onSuccess(userProfile);
            return;
        }

        final GetUserCallback.GetUserResponse getUserResponse = new GetUserCallback.GetUserResponse() {
            @Override
            public void onCompleted(@NonNull SocialMember socialMember) {
                callback.onSuccess(socialMember);
            }
        };
        UserRequest.makeUserRequest(new GetUserCallback(getUserResponse).getCallback());
    }
}
