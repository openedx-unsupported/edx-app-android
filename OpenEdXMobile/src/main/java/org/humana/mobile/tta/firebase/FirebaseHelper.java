package org.humana.mobile.tta.firebase;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import static org.humana.mobile.util.BrowserUtil.loginPrefs;

public  class FirebaseHelper {

    public static void updateFirebasetokenToServer(Context context) {

        if(loginPrefs==null || loginPrefs.getUsername()==null || loginPrefs.getUsername().equals(""))
            return;

        try {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                String mToken = instanceIdResult.getToken();
                if (mToken == null)
                    return;

                Log.d("FA Token","FirebaseHelper Token--->"+ mToken);

                Bundle parameters = new Bundle();
                parameters.putString("user_id", loginPrefs.getUsername());
                parameters.putString("token", mToken);

                FirebaseTokenUpdateTask firebasetokn_update_task = new FirebaseTokenUpdateTask(context, parameters) {
                    @Override
                    public void onSuccess(@NonNull FirebaseUpdateTokenResponse result) {
                        Log.d("FA Token ","FirebaseHelper Token sync success--->"+ mToken);
                    }

                    @Override
                    public void onException(Exception ex) {
                        Log.d("FA Token","FirebaseHelper Token sync fail--->"+ mToken);
                    }
                };
                firebasetokn_update_task.execute();
            });
        }
        catch (Exception ex)
        {
            Log.e("FirebaseHelper ex", ex.getMessage());
        }
    }
}
