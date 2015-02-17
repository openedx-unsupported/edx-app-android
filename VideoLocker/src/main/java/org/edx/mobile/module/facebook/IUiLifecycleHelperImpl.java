package org.edx.mobile.module.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

/**
 * Created by rohan on 2/12/15.
 *
 * This class forwards all the methods calls to {@link com.facebook.UiLifecycleHelper}.
 * However, if during object creation of this class, if Facebook applicationId was not configured, then
 * this class doesn't forward any calls to {@link com.facebook.UiLifecycleHelper}, meaning it does nothing in this case.
 */
class IUiLifecycleHelperImpl implements IUiLifecycleHelper {

    private UiLifecycleHelper uiLifecycleHelper;

    public IUiLifecycleHelperImpl(Activity activity, Session.StatusCallback statusCallback) {
        if (com.facebook.Settings.getApplicationId() != null) {
            // make this initialization only if there is an applicationId already configured

            uiLifecycleHelper = new UiLifecycleHelper(activity, statusCallback);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onResume();
    }

    @Override
    public void onPause() {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onPause();
    }

    @Override
    public void onStop() {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onStop();
    }

    @Override
    public void onDestroy() {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onDestroy();
    }

    @Override
    public void trackPendingDialogCall(FacebookDialog.PendingCall present) {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.trackPendingDialogCall(present);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, FacebookDialog.Callback callback) {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onActivityResult(requestCode, resultCode, data, callback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (uiLifecycleHelper != null)
            uiLifecycleHelper.onSaveInstanceState(outState);
    }
}
