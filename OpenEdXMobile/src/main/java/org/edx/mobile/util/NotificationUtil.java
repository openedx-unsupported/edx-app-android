package org.edx.mobile.util;

import com.google.firebase.messaging.FirebaseMessaging;

import org.edx.mobile.logger.Logger;

public class NotificationUtil {
    final static Logger logger = new Logger(NotificationUtil.class.getName());
    public static final String NOTIFICATION_TOPIC_RELEASE = "release_notification_android";

    /**
     * Subscribe to the Topic channels that will be used to send Group notifications
     */
    public static void subscribeToTopics(Config config) {
        if (config.areFirebasePushNotificationsEnabled()) {
            FirebaseMessaging.getInstance().subscribeToTopic(
                NOTIFICATION_TOPIC_RELEASE
            );
        }
    }

    /**
     * UnSubscribe from all the Topic channels
     */
    public static void unsubscribeFromTopics(Config config) {
        if (config.getFirebaseConfig().isEnabled()) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(
                NOTIFICATION_TOPIC_RELEASE
            );
        }
    }
}
