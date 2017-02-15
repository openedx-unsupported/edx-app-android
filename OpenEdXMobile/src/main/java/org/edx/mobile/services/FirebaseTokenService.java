package org.edx.mobile.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import android.content.SharedPreferences;

import org.edx.mobile.util.KonnekteerUtil;

public class FirebaseTokenService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Create the mobile endpoint for the device
        KonnekteerUtil.createMobileEndpoint(this);
    }
}