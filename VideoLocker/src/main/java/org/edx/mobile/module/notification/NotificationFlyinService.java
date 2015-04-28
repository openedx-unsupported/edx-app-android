package org.edx.mobile.module.notification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationFlyinService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
         return null;
    }
}

// the fly-in notification window requires additional uses-permission.
//  we should not ask user for it unless we do use it


//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.PixelFormat;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.TextView;
//
//import org.edx.mobile.R;
//import org.edx.mobile.logger.Logger;
//import org.edx.mobile.util.ResourceUtil;
//import org.edx.mobile.util.UiUtil;
//import org.edx.mobile.view.Router;
//
///**
// * before Android 5, flyin notification window is not supported.
// */
//public class NotificationFlyinService extends Service {
//    protected final Logger logger = new Logger(getClass().getName());
//
//    private WindowManager windowManager;
//
//    View popupView;
//    TextView messageTitle;
//    TextView messageBody;
//    CourseUpdateNotificationPayload payload;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//         return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
//        popupView = layoutInflater.inflate(R.layout.panel_flyin_message, null);
//        messageTitle = (TextView) popupView.findViewById(R.id.message_title);
//        messageBody = (TextView) popupView.findViewById(R.id.message_body);
//
//
//        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_PHONE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.OPAQUE);
//
//        params.gravity = Gravity.TOP | Gravity.CENTER;
//        params.x = 0;
//        params.y = 0;
//
//        windowManager.addView(popupView, params);
//
//        try {
//            popupView.setOnTouchListener(new View.OnTouchListener() {
//
//                @Override public boolean onTouch(View v, MotionEvent event) {
//                    if( payload != null  ) {
//                        java.lang.Class<? extends android.app.Activity> aclass = ParseHandleHelper.getActivityClass(payload);
//                        Intent intent = new Intent(getApplicationContext(), aclass);
//
//                        Bundle courseBundle = new Bundle();
//                        courseBundle.putBoolean(Router.EXTRA_ANNOUNCEMENTS, true);
//                        courseBundle.putString(Router.EXTRA_COURSE_ID, payload.getCourseId());
//
//                        //if user launch the app from recent list, activity will still get this intent.
//                        //this is one way to avoid it.
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                        intent.putExtra( Router.EXTRA_BUNDLE, courseBundle);
//                        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                        try {
//                            pi.send();
//                            NotificationManager notificationManager =
//                                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                            notificationManager.cancelAll();
//                        } catch (PendingIntent.CanceledException e) {
//                            logger.error(e);
//                        }
//                    }
//                    return false;
//                }
//            });
//        } catch (Exception e) {
//             logger.error(e);
//        }
//
//
//    }
//
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        String data=(String) intent.getExtras().get("com.parse.Data");
//        payload = ParseHandleHelper.extractPayload(intent);
//        if( payload != null  ) {
//            String titleTemplate =  ResourceUtil.getResourceString(R.string.COURSE_ANNOUNCEMENT_NOTIFICATION_TITLE);
//            messageTitle.setText(titleTemplate);
//            messageBody.setText(payload.getCourseName());
//            UiUtil.animateLayouts(popupView);
//        }
//        return flags;
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if ( popupView  != null)
//            windowManager.removeView(popupView);
//    }
//
//}