package org.edx.mobile.services;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import org.edx.mobile.R;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.Context;
import android.app.Notification;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.SplashActivity;

public class FirebaseNotificationsService extends FirebaseMessagingService {

    private final Logger logger = new Logger(getClass().getName());

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String messageText = remoteMessage.getData().get("default");

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.small_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(getString(R.string.platform_name))
                        .setContentText(messageText)
                        .setStyle(new Notification.BigTextStyle().bigText(messageText));

        Intent resultIntent = new Intent(this, SplashActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SplashActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}