package org.humana.mobile.tta.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.inject.Inject;

import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.enums.NotificationType;
import org.humana.mobile.tta.data.local.db.table.Notification;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.programs.notifications.NotificationActivity;
import org.humana.mobile.tta.ui.programs.notifications.NotificationNavigationActivity;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;
import org.humana.mobile.tta.ui.splash.SplashActivity;
import org.humana.mobile.tta.utils.NotificationUtil;

import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

import static org.humana.mobile.util.BrowserUtil.loginPrefs;

public class TaFirebaseMessagingService extends FirebaseMessagingService {

    public static final String COURSE = "course";
    public static final String CONNECT = "connect";
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_CONNECT_URL = "connect_url";

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_ISPUSH = "isPush";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_SECTION_ID = "section_id";
    public static final String EXTRA_PARENT_ACTION_ID = "action_parent_id";
    public static final String EXTRA_ACTION_ID = "action_id";
    DataManager mDataManager;
    EnrolledCoursesResponse coursesResponse;

    @Inject
    IEdxEnvironment environment;

    Intent navigationIntent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Log.d("Manprax","Notification Message Body: " + remoteMessage);
        //sendNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"),"","");
        mDataManager = DataManager.getInstance(this);

        if (remoteMessage.getData().containsKey(EXTRA_TITLE) && remoteMessage.getData().containsKey("body") &&
                remoteMessage.getData().containsKey("path") && remoteMessage.getData().containsKey("type"))
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"),
                    remoteMessage.getData().get("path"), remoteMessage.getData().get("type"),
                    remoteMessage.getData().get(EXTRA_PARENT_ACTION_ID),
                    remoteMessage.getData().get(EXTRA_ACTION_ID));
        else if (remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("body"))
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"),
                    "", "", "", "");
        else
            return;
    }

    private void sendNotification(String messageTitle, String messageBody, String path, String type,
                                  String action_parent_id, String action_id) {
        //**add this line**
        int requestID = (int) System.currentTimeMillis();

        Intent notificationIntent;

        //get Intent for notification landing page
        //remove this for generlisation && path!=null && !path.equals("")
        if (type != null && !type.equals("")) {
            notificationIntent = getNavigationIntent(type, path, action_parent_id, action_id);
        } else {
            notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        }

        //**add this line**
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestID,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int notificationId = 1;
        String channelId = "TheTeacherapp-Channel-01";
        String channelName = "TheTeacherapp Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        new NotificationUtil(this, notificationId, channelId, channelName,
                channelName, importance)
                .setTitle(messageTitle)
                .setMessage(messageBody)
                .setContentIntent(contentIntent)
                .show();

        if (loginPrefs.getUsername() != null) {
            DataManager dataManager = DataManager.getInstance(getApplicationContext());

            Notification notification = new Notification();
            notification.setCreated_time(System.currentTimeMillis());
            notification.setDescription(messageBody);
            notification.setTitle(messageTitle);
            notification.setUsername(loginPrefs.getUsername());
            notification.setType(NotificationType.content.name());
            notification.setRef_id(path);

            // dataManager.createNotification(notification);
        }

        loginPrefs.setNotificationSeen(Constants.NOTIFICATION_RECIEVED);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {

            if (!task.isSuccessful())
                return;

            Log.d("Firebase Token", "TaFirebaseMessagingService Token-->" +
                    "" + task.getResult().getToken());

            FirebaseHelper.updateFirebasetokenToServer(getApplicationContext());
        });
    }

    private Intent getNavigationIntent(String type, String path, String action_parent_id, String action_id) {
        navigationIntent = new Intent();

        //if user is not logged in navigate to splash screen
        //removed this check "|| (path==null && path.equals(""))"
        if (type == null || type.equals("") || loginPrefs == null || loginPrefs.getUsername() == null || loginPrefs.getUsername().equals("")) {
            navigationIntent = new Intent(getApplicationContext(), NotificationActivity.class);
            //return here default intent for dashboard
            return navigationIntent;
        }

        if (type.equals(COURSE) || type.equals(CONNECT)) {
            if (path == null || path.equals(""))
                navigationIntent = new Intent(getApplicationContext(), NotificationActivity.class);
            else {
                navigationIntent = new Intent(getApplicationContext(), NotificationActivity.class);
                navigationIntent.putExtra(EXTRA_PATH, path);
                navigationIntent.putExtra(EXTRA_TYPE, type);
                navigationIntent.putExtra(EXTRA_ISPUSH, true);
            }
        } else {
            navigationIntent = new Intent(getApplicationContext(), NotificationNavigationActivity.class);
            navigationIntent.putExtra(EXTRA_PARENT_ACTION_ID, action_parent_id);
            navigationIntent.putExtra(EXTRA_ACTION_ID, action_id);
        }
        return navigationIntent;
    }

}
