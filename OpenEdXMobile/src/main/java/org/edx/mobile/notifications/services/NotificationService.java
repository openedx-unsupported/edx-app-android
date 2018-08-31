package org.edx.mobile.notifications.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.SplashActivity;


public class NotificationService extends FirebaseMessagingService {
    public static final String NOTIFICATION_TOPIC_RELEASE = "release_notification_android";
    public static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "default_openedx_notification_channel";

    private static final int NOTIFICATION_ID = 999;
    protected static final Logger logger = new Logger(NotificationService.class.getName());

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }

    @Override
    public void onNewToken(String s) {
        logger.debug("Refreshed FCM token: " + s);
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final IEdxEnvironment environment = MainApplication.getEnvironment(this);

        if (!environment.getConfig().areFirebasePushNotificationsEnabled()) {
            // Do not process Notifications when they are disabled.
            return;
        }

        if (remoteMessage.getNotification() != null) {
            logger.debug(
                    "Message Notification Body: " + remoteMessage.getNotification().getBody()
            );
        }

        // Send the message upstream to generate a system notification
        sendNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody()
        );
    }

    /**
     * Create and show a simple notification containing the message data.
     */
    private void sendNotification(String title, String messageBody) {
        final Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Build out the Notification and set the intent to direct the user to the application
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    DEFAULT_NOTIFICATION_CHANNEL_ID,
                    getResources().getString(R.string.default_notification_channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            try {
                notificationManager.createNotificationChannel(channel);
            } catch (NullPointerException ex) {
                logger.error(ex);
                return;
            }
        }
        try {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        } catch (NullPointerException ex) {
            logger.error(ex);
            return;
        }
    }
}
