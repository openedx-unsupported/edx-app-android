package org.edx.mobile.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import android.content.SharedPreferences;

import org.edx.mobile.util.KonnekteerUtil;

public class FirebaseTokenService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        KonnekteerUtil.createMobileEndpoint(this);
    }
}