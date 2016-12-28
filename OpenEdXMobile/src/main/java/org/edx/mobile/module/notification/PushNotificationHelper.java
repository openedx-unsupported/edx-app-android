package org.edx.mobile.module.notification;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.view.MyCoursesListActivity;
import org.edx.mobile.view.Router;

/**
 * Common helper for Parse Notification
 */
public class PushNotificationHelper {
    public static final String COURSE_ANNOUNCEMENT_ACTION = "course.announcement";

    private static final Logger logger = new Logger(PushNotificationHelper.class.getName());

    @Nullable
    public static BaseNotificationPayload extractPayload(@NonNull String payloadStr) {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(payloadStr, CourseUpdateNotificationPayload.class);
        } catch (JsonSyntaxException ex) {
            return null;
        }
    }

    public static void onClickNotification(Context context, AnalyticsRegistry analyticsRegistry, Router router, BaseNotificationPayload payload) {
        if (null != payload && null != payload.getAction()) {
            switch (payload.getAction()) {
                case PushNotificationHelper.COURSE_ANNOUNCEMENT_ACTION: {
                    final String courseId = ((CourseUpdateNotificationPayload) payload).getCourseId();
                    analyticsRegistry.trackNotificationTapped(courseId);
                    router.showCourseAnnouncementFromNotification(context, courseId);
                    return;
                }
            }
        }

        // Default behaviour for unknown notification types
        analyticsRegistry.trackNotificationTapped(null);
        context.startActivity(new Intent(context, MyCoursesListActivity.class));
    }

    public static boolean hasNotificationHash(Context context, String notificationId) {
        PrefManager.AppInfoPrefManager pmanager = new PrefManager.AppInfoPrefManager(context);
        String prevHashCode = pmanager.getPrevNotificationHashKey();
        pmanager.setPrevNotificationHashKey(notificationId);
        if (TextUtils.isEmpty(notificationId) && TextUtils.isEmpty(prevHashCode))
            return true;
        if (notificationId != null && notificationId.equals(prevHashCode))
            return true;
        return false;
    }

}
