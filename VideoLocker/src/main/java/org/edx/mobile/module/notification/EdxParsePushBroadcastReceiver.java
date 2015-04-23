package org.edx.mobile.module.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.ParsePushBroadcastReceiver;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import org.edx.mobile.R;
import org.edx.mobile.event.CourseAnnouncementEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.CourseDetailTabActivity;
import org.edx.mobile.view.MyCoursesListActivity;
import org.edx.mobile.view.Router;

import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * subclass ParsePushBroadcastReceiver to provide fine control of
 * app's behavior on receiving the notification message
 */
public class EdxParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    protected final Logger logger = new Logger(getClass().getName());

    public void onReceive(android.content.Context context, android.content.Intent intent) {
        super.onReceive(context,intent);
     }

    private CourseUpdateNotificationPayload extractPayload(android.content.Intent intent) {
        try {
            String payloadStr = intent.getExtras().getString("com.parse.Data");
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(payloadStr, CourseUpdateNotificationPayload.class);
        } catch (Exception ex) {
            logger.debug(ex.toString());
            return null;
        }
    }

    private java.lang.Class<? extends android.app.Activity> getActivityClass(CourseUpdateNotificationPayload payload){
        if ( payload == null ){
            return MyCoursesListActivity.class;
        }
        List<String> args = payload.getLocArgs();
        if ( args != null && args.size() > 0 && UserNotificationManager.appCanHandleFormat(payload)) {
            return CourseDetailTabActivity.class;
        }
        return MyCoursesListActivity.class;
    }

    protected void onPushReceive(android.content.Context context, android.content.Intent intent) {
        super.onPushReceive(context, intent);
    }


    protected void onPushDismiss(android.content.Context context, android.content.Intent intent) {
        super.onPushDismiss(context, intent);
    }

    protected void onPushOpen(android.content.Context context, android.content.Intent intent) {
        super.onPushOpen(context, intent);
    }

    protected java.lang.Class<? extends android.app.Activity> getActivity(android.content.Context context, android.content.Intent intent) {
        CourseUpdateNotificationPayload payload = extractPayload(intent);
        return getActivityClass(payload);
    }

    protected android.app.Notification getNotification(android.content.Context context, android.content.Intent intent) {
        try {

            CourseUpdateNotificationPayload payload = extractPayload(intent);
            Class activity = getActivityClass(payload);
            if( activity == CourseDetailTabActivity.class ){
                String titleTemplateName = payload.getTitleLocKey();
                String titleTemplate = "";
                if ( titleTemplateName != null) {
                    try {
                        titleTemplate = ResourceUtil.getResourceString(titleTemplateName);
                    }catch (Exception ex){
                        logger.error(ex);
                    }
                }
                List<String> args = payload.getLocArgs();
                EventBus.getDefault().postSticky(new CourseAnnouncementEvent(
                        CourseAnnouncementEvent.EventType.MESSAGE_RECEIVED, payload.getCourseId()));

                String alert = args.get(0);
                payload.setAlert(alert);

                Bundle courseBundle = new Bundle();
                courseBundle.putBoolean(Router.EXTRA_ANNOUNCEMENTS, true);
                courseBundle.putString(Router.EXTRA_COURSE_ID, payload.getCourseId());


                Intent resultIntent = new Intent(context, CourseDetailTabActivity.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent.putExtra( Router.EXTRA_BUNDLE, courseBundle);

                // Because clicking the notification opens a new ("special") activity, there's
                // no need to create an artificial back stack.
                PendingIntent resultPendingIntent =  PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        0
                );

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle(titleTemplate);
                builder.setContentText(payload.getAlert());
                builder.setSmallIcon(R.drawable.app_icon);
                builder.setContentIntent(resultPendingIntent);
                builder.setAutoCancel(true);

                Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(uri);

               // int mNotificationId = RemoteNotificationTypes.COURSE_ANNOUNCEMENT.ordinal();
                //notificationManager.notify(mNotificationId, builder.build());
                return builder.build();
            } else {
                return super.getNotification(context,intent);
            }

        } catch (Exception e) {
            return super.getNotification(context, intent);
        }
    }
}
