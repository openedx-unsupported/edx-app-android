package org.edx.mobile.social.facebook;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;

import com.facebook.Session;
import com.facebook.SessionState;

import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.social.ISocialImpl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FacebookAuth extends ISocialImpl {

    private IUiLifecycleHelper uiHelper;

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    public FacebookAuth(Activity activity) {
        super(activity);
    }

    @Override
    public void login() {
        Session session = Session.getActiveSession();
        if (session != null && !session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(activity.get())
                    .setPermissions(Arrays.asList("public_profile", "email"))
                    .setCallback(statusCallback));
        } else {
            Session.openActiveSession(activity.get(), true, statusCallback);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        uiHelper = IUiLifecycleHelper.Factory.getInstance(activity, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        keyHash();
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        uiHelper.onPause();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    private void onSessionStateChange(Session session, SessionState state,
            Exception exception) {
        if (state.isOpened()) {
            if (callback != null) {
                callback.onLogin(session.getAccessToken());
            }
            logger.debug("Facebook Logged in...");
        } else if (state.isClosed()) {
            logger.debug("Facebook Logged out...");
        } else {
            logger.debug("Facebook state changed ...");
        }
    }

    private void keyHash() {
        try {
            PackageInfo info = activity
                    .get()
                    .getPackageManager()
                    .getPackageInfo(activity.get().getPackageName(),
                            PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                logger.debug("Facebook Key Hash:"+ Base64.encodeToString(md.digest(),
                        Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
            logger.error(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        }
    }

    @Override
    public void logout() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
                //clear your preferences if saved
            }
        } else {
            session = new Session(activity.get());
            Session.setActiveSession(session);

            session.closeAndClearTokenInformation();
            //clear your preferences if saved
        }
        
        logger.debug("facebook logged out");
    }

}
