package org.edx.mobile.module.notification;

import com.parse.ParsePushBroadcastReceiver;

import org.edx.mobile.view.CourseDetailTabActivity;
import org.edx.mobile.view.MyCoursesListActivity;

/**
 * subclass ParsePushBroadcastReceiver to provide fine control of
 * app's behavior on receiving the notification message
 */
public class EdxParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public void onReceive(android.content.Context context, android.content.Intent intent) {
        super.onReceive(context,intent);
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
        return MyCoursesListActivity.class;
    }

    protected android.app.Notification getNotification(android.content.Context context, android.content.Intent intent) {
        return super.getNotification(context,intent);
    }
}
