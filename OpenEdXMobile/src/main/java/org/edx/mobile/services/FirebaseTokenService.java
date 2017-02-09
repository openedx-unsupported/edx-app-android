package org.edx.mobile.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import android.content.SharedPreferences;

import org.edx.mobile.util.KonnekteerUtil;

public class FirebaseTokenService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences settings = getSharedPreferences("pushnotifications", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", refreshedToken);
        editor.apply();
        // Create the mobile endpoint for the device token
        KonnekteerUtil.createMobileEndpoint(this);
    }
}