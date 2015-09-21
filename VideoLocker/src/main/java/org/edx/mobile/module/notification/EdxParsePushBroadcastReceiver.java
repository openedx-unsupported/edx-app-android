package org.edx.mobile.module.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.inject.Inject;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.view.MyCoursesListActivity;
import org.edx.mobile.view.Router;

import java.util.Random;

/**
 * subclass ParsePushBroadcastReceiver to provide fine control of
 * app's behavior on receiving the notification message
 */
public class EdxParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Inject
    ISegment segment;

    @Inject
    Router router;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((MainApplication) context.getApplicationContext()).getInjector().injectMembers(this);
        super.onReceive(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        BaseNotificationPayload payload = ParseHandleHelper.extractPayload(intent);
        if (payload != null && ParseHandleHelper.hasNotificationHash(context, payload.getIdentifier()))
            return;
        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);

        final BaseNotificationPayload payload = ParseHandleHelper.extractPayload(intent);
        if (null != payload && null != payload.getAction()) {
            switch (payload.getAction()) {
                case ParseHandleHelper.COURSE_ANNOUNCEMENT_ACTION: {
                    final String courseId = ((CourseUpdateNotificationPayload) payload).getCourseId();
                    segment.trackNotificationTapped(courseId);
                    router.showCourseAnnouncementFromNotification(context, courseId);
                    return;
                }
            }
        }

        // Default behaviour for unknown notification types
        segment.trackNotificationTapped(null);
        context.startActivity(new Intent(context, MyCoursesListActivity.class));
    }

    @Override
    protected android.app.Notification getNotification(@NonNull Context context, @NonNull Intent intent) {
        final BaseNotificationPayload payload = ParseHandleHelper.extractPayload(intent);
        if (null != payload && null != payload.getAction()) {
            switch (payload.getAction()) {
                case ParseHandleHelper.COURSE_ANNOUNCEMENT_ACTION: {
                    final CourseUpdateNotificationPayload courseUpdateNotificationPayload = (CourseUpdateNotificationPayload) payload;
                    segment.trackNotificationReceived(courseUpdateNotificationPayload.getCourseId());
                    return newNotificationBuilder(context, intent)
                            .setContentTitle(context.getString(R.string.COURSE_ANNOUNCEMENT_NOTIFICATION_TITLE))
                            .setContentText(courseUpdateNotificationPayload.getCourseName())
                            .build();
                }
            }
        }

        // Default behaviour for unknown notification types
        segment.trackNotificationReceived(null);
        return super.getNotification(context, intent);
    }

    private NotificationCompat.Builder newNotificationBuilder(Context context, Intent intent) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        /***
         * The following block of code was copied from {@link ParsePushBroadcastReceiver#getNotification(Context, Intent)}
         * It sets the OPEN and DELETE intents so that onPushOpen and onPushDismiss are called.
         */
        {

            final Bundle extras = intent.getExtras();

            // Security consideration: To protect the app from tampering, we require that intent filters
            // not be exported. To protect the app from information leaks, we restrict the packages which
            // may intercept the push intents.
            final String packageName = context.getPackageName();

            final Intent contentIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
            contentIntent.putExtras(extras);
            contentIntent.setPackage(packageName);

            final Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
            deleteIntent.putExtras(extras);
            deleteIntent.setPackage(packageName);

            Random random = new Random();
            int contentIntentRequestCode = random.nextInt();
            int deleteIntentRequestCode = random.nextInt();

            final PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                    contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            final PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                    deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pContentIntent);
            builder.setDeleteIntent(pDeleteIntent);
        }
        return builder;
    }
}
