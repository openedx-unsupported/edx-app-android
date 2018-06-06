package org.edx.mobile.util;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.notifications.services.NotificationService;

public class NotificationUtil {
    final static Logger logger = new Logger(NotificationUtil.class.getName());

    /**
     * Subscribe to the Topic channels that will be used to send Group notifications
     */
    public static void subscribeToTopics(Config config) {
        if (config.areFirebasePushNotificationsEnabled()) {
            FirebaseMessaging.getInstance().subscribeToTopic(
                    NotificationService.NOTIFICATION_TOPIC_RELEASE
            );
        }
    }

    public static void logFirebaseToken() {
        // Get token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            logger.warn("getInstanceId failed");
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        logger.debug("Firebase Token: " + token);
                    }
                });
    }
}
