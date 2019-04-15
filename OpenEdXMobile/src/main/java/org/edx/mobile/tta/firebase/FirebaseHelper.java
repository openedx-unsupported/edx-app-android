package org.edx.mobile.tta.firebase;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;

import org.edx.mobile.util.Config;

import java.util.List;

import static org.edx.mobile.util.BrowserUtil.config;

public class FirebaseHelper {
    public static final String NOTIFICATION_TOPIC_RELEASE = "release_notification_android";

    private  TaFirebaseInstanceIDService myFirebaseInstanceIDService=new TaFirebaseInstanceIDService();
    private Bundle parameters = new Bundle();

    public Bundle getFireBaseParams(String user_id) {

        //String device_info="API Level:"+ Build.VERSION.RELEASE+"  Device:"+Build.DEVICE+"  Model no:"+Build.MODEL+"  Product:"+Build.PRODUCT;

        parameters.putString("user_id",user_id);
        parameters.putString("token_id",myFirebaseInstanceIDService.getFireBaseToken());
        //parameters.putString("device_info",device_info);

        return parameters;
    }

    public void updateFirebasetokenToServer(Context context, Bundle parameters) {

        if(myFirebaseInstanceIDService.getFireBaseToken()==null)
            return;

        FirebaseTokenUpdateTask firebasetokn_update_task = new FirebaseTokenUpdateTask(context,parameters) {
            @Override
            public void onSuccess(@NonNull FirebaseUpdateTokenResponse result) {
            }
            @Override
            public void onException(Exception ex) {
            }
        };
        firebasetokn_update_task.execute();
    }

    public static void subscribeToTopics(@NonNull List<String> topics){
        for (String topic: topics){
            subscribeToTopic(topic);
        }
    }

    public static void subscribeToTopic(String topic) {
        if (config.areFirebasePushNotificationsEnabled()) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }

    public static void unsubscribeFromTopics(@NonNull List<String> topics){
        for (String topic: topics){
            unsubscribeFromTopic(topic);
        }
    }

    public static void unsubscribeFromTopic(String topic) {
        if (config.getFirebaseConfig().isEnabled()) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        }
    }

}
