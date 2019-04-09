package org.edx.mobile.tta.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class NotificationUtil {

    private static final int MAX_PROGRESS = 100;
    private static final String DEFAULT_NOTIFICATION_TITLE = "TTA";
    private static final String DEFAULT_NOTIFICATION_MESSAGE = "TTA default notification";
    private static final String DEFAULT_CHANNEL_ID = "tta_default_notification_channel_id";
    private static final String DEFAULT_CHANNEL_NAME = "TTA default channel";

    private Context context;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;

    private int notificationId;
    private String channelId, channelName, channelDescription;
    private int channelImportance;

    public NotificationUtil(Context context, int notificationId) {
        this.context = context;
        this.notificationId = notificationId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = DEFAULT_CHANNEL_ID;
            channelName = DEFAULT_CHANNEL_NAME;
            channelDescription = DEFAULT_CHANNEL_NAME;
            channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
        }
        init();
    }

    public NotificationUtil(Context context, int notificationId, String channelId, String channelName,
                            String channelDescription, int channelImportance) {
        this.context = context;
        this.notificationId = notificationId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelDescription = channelDescription;
        this.channelImportance = channelImportance;
        init();
    }

    private void init(){
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new Notification.Builder(context);
        setOngoing(false).setTitle(DEFAULT_NOTIFICATION_TITLE).setMessage(DEFAULT_NOTIFICATION_MESSAGE)
                .setNotificationIcon(context.getApplicationInfo().icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
        channel.setDescription(channelDescription);

        notificationManager.createNotificationChannel(channel);
        notificationBuilder.setChannelId(channelId);

    }

    public NotificationUtil setOngoing(boolean isOngoing){
        notificationBuilder.setOngoing(isOngoing);
        return this;
    }

    public NotificationUtil setTitle(String title){
        notificationBuilder.setContentTitle(title);
        return this;
    }

    public NotificationUtil setMessage(String message){
        notificationBuilder.setContentText(message);
        return this;
    }

    public NotificationUtil setNotificationIcon(int icon){
        notificationBuilder.setSmallIcon(icon);
        return this;
    }

    public NotificationUtil setProgress(int progressOutOf100, boolean indeterminate){
        notificationBuilder.setProgress(MAX_PROGRESS, progressOutOf100, indeterminate);
        return this;
    }

    public NotificationUtil setContentIntent(PendingIntent intent){
        notificationBuilder.setContentIntent(intent);
        return this;
    }

    public void show(){
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void dismiss(){
        notificationManager.cancel(notificationId);
    }

    public static void dismissNotification(Activity activity, int notificationId){
        ((NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);
    }
}
