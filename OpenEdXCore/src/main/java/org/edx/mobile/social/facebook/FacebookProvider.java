package org.edx.mobile.social.facebook;

import android.content.Context;
import android.os.Bundle;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
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

    /**
     * returns true if there was an error. The callback will be notified
     */
    private boolean notifyIfError(Response response, Callback callback) {
        if (response.getError() != null) {
            FacebookRequestError error = response.getError();
            callback.onError(new SocialError(error.getException()));
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

        Session session = Session.getActiveSession();
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (!notifyIfError(response, callback)) {
                    logger.debug(user.getUsername() + ":" + user.getName() + ":" + user.getFirstName()
                            + ":" + user.getLastName() + ":" + user.getId());
                    logger.debug(user.getProperty("email") + "");
                    SocialMember member = new SocialMember(Long.parseLong(user.getId()), user.getName());
                    member.setEmail(user.getProperty("email") + "");
                    callback.onSuccess(member);
                }
            }
        });
        Bundle params = request.getParameters();
        params.putString("fields", "email,id,name");
        request.setParameters(params);
        request.executeAsync();
    }
}
