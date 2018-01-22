package org.edx.mobile.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.view.CourseUnitNavigationActivity;
import org.edx.mobile.view.Router;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by arslan on 1/9/18.
 */

public class AudioMediaService extends Service implements IPlayerListener{

    private static final String TAG = "TEST_TAG_SERVICE";
    Notification notification;
    NotificationCompat.Builder notificationBuilder;
    Intent notificationIntent, playIntent, pauseIntent, deleteIntent, cancelIntent;
    PendingIntent notificationPendingIntent, playPendingIntent, pausePendingIntent, deletePendingIntent, cancelPendingIntent;
    NotificationManager notificationManager;
    RemoteViews notificationRemoteView;
    IBinder iBinder = new MusicBinder();
    boolean isRunningForeground = false;
    boolean isBound = false;
    boolean isNotificationShowing = false;
    public static final String MUSIC_CHANNEL = "MusicChannel";
    String notificationTitle, notificationMessage;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;


    public static final String ACTION_MAIN_ACTIVITY = "MAIN_ACTIVITY";
    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String START_SERVICE = "START_SERVICE";
    public static final String STOP_SERVICE = "STOP_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";

    public static final String PLAY_MEDIA = "PLAY_MEDIA";
    public static final String PAUSE_MEDIA = "PAUSE_MEDIA";
    public static final String NEXT_MEDIA = "NEXT_MEDIA";
    public static final String PREVIOUS_MEDIA = "PREVIOUS_MEDIA";
    public static final String AUDIO_URI_ID = "URI";
    public static final String AUDIO_NOTIFICATION_TITLE = "notification_title";
    public static final String AUDIO_NOTIFICATION_MESSAGE = "notification_message";
    public static final String COURSE_DATA_BUNDLE = "courseDataBundle";

    public static final String DELETE_INTENT = "DELETE_INTENT";
    public static final String CANCEL_INTENT = "CANCEL";
    public static int NOTIFICATION_ID = 101;

    IPlayerListener iPlayerListenerActivityCallbacks;
    IPlayer currentPlayer;

    HashMap<String , Player> connectedPlayers= new HashMap<>();
    @Override
    public void onCreate() {

        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        android.os.Debug.waitForDebugger();
//        player = new Player();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG , "START COMMAND");
        if (intent != null)
        {
            String action = intent.getAction() == null ? "" : intent.getAction();
            //Stop the notification if already shown
            if(action.equals(START_SERVICE)) {
                stopForegroundService();
            }

            //Play button clicked in notification
            else if (action.equals(PLAY_MEDIA)) {
                handleResumeAction(false);
                Log.d(TAG , "RESUME/PLAY COMMAND");
            }

            //Pause button clicked in notification
            else if (action.equals(PAUSE_MEDIA)) {
                handlePauseAction(false);
                Log.d(TAG , "PAUSE COMMAND");

            }

            //Cancel button clicked in notification
            else if(action.equals(CANCEL_INTENT))
            {
                Log.d(TAG , "CANCEL COMMAND");

                currentPlayer.reset();
                currentPlayer.release();
                currentPlayer = null;
                connectedPlayers.clear();
                stopForegroundService();
                stopSelf();
            }
            else if(action.equals(DELETE_INTENT))
            {
                Log.d(TAG , "DELETE COMMAND");

                if(!isBound)
                {
                    stopSelf();
                    Log.d(TAG , "STOP SERVICE COMMAND");

                }
                else{
                    stopForegroundService();
                    isNotificationShowing = false;
                    Log.d(TAG , "STOP FG COMMAND");

                }
            }
            initializeIntents();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG , "ON BIND");

        isBound = true;
//        startForegroundService();
        return iBinder;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG , "ON UNBIND");

        isBound = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG , "ON DESTROY");
    }
    /**This will create a notification in notification bar with current music play state**/
    public void startForegroundService()
    {
        initializeCustomNotification();
        startForeground(NOTIFICATION_ID,notification);
        if(currentPlayer != null){
            currentPlayer.setPlayerListener(this);
        }
    }
    /**This will cancel/hide the notification in notification bar**/
    public void stopForegroundService()
    {
        stopForeground(true);
        isNotificationShowing = false;
        isRunningForeground = false;
    }

    public void setPlayerCallbacks(IPlayerListener iplayerListener)
    {
        this.iPlayerListenerActivityCallbacks = iplayerListener;
    }


    /**This will update the notification with resume state/pause button**/
    public void showResumeNotification()
    {
        notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.ic_pause_grey600_18dp);
        notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, pausePendingIntent);
        notificationBuilder.setCustomContentView(notificationRemoteView);
        notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID , notification);
        isNotificationShowing = true;
    }

    /**This will update the notification with play state/play button**/
    public void showPauseNotification()
    {
        notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.ic_play_grey600_18dp);
        notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
        notificationBuilder.setCustomContentView(notificationRemoteView);
        notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID , notification);
        isNotificationShowing = true;
    }

    /**
     *
     * @param fromActivity
     * Handles the pause button click even clicked through notification or Activity
     */
    public void handlePauseAction(boolean fromActivity)
    {
//        if (isNotificationShowing)
        showPauseNotification();
        currentPlayer.pause();

//        if(!fromActivity) activityCallbacks.onMediaPaused();
    }

    /**
     *
     * Handles the resume/play button click even clicked through notification or Activity
     */
    public void handleResumeAction(boolean fromActivity)
    {
//        if(isNotificationShowing)
        showResumeNotification();
        currentPlayer.start();
//        if(!fromActivity) activityCallbacks.onMediaResumed();

    }


    /**
     * Initialize all Intents/PendingIntents for handling service call actions
     */
    private void initializeIntents()
    {
        notificationIntent = new Intent(this, CourseUnitNavigationActivity.class);
        notificationIntent.setAction(ACTION_MAIN_ACTIVITY);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        notificationIntent.putExtra(Router.EXTRA_BUNDLE, createCourseBundle());
        notificationIntent.putExtra(Router.EXTRA_IS_VIDEOS_MODE, false);

        notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        playIntent = new Intent(this, AudioMediaService.class);
        playIntent.setAction(PLAY_MEDIA);
        playPendingIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        pauseIntent = new Intent(this, AudioMediaService.class);
        pauseIntent.setAction(PAUSE_MEDIA);
        pausePendingIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);
        deleteIntent = new Intent(this, AudioMediaService.class);
        deleteIntent.setAction(DELETE_INTENT);
        deletePendingIntent = PendingIntent.getService(this, 0,
                deleteIntent, 0);

        cancelIntent = new Intent(this, AudioMediaService.class);
        cancelIntent.setAction(CANCEL_INTENT);
        cancelPendingIntent = PendingIntent.getService(this, 0,
                cancelIntent, 0);

    }

    private Bundle createCourseBundle()
    {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        courseBundle.putSerializable(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        return courseBundle;
    }

    /**
     * Initialize the notification to be show if user closes the application while playing audio
     */
    private void initializeCustomNotification()
    {
        initializeRemoteView();
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.launch_screen_logo);
        notificationBuilder  = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(true);
        notification = notificationBuilder.setContentTitle("Test Music Player")
                .setSmallIcon(R.drawable.launch_screen_logo)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 100, 70, false))
                .setContentIntent(notificationPendingIntent)
                .setCustomContentView(notificationRemoteView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setDeleteIntent(deletePendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
    }

    /**
     * Initialize the Remote view for the custom notification display layout
     */
    private void initializeRemoteView()
    {
        notificationRemoteView = new RemoteViews(getPackageName() , R.layout.notification_layout);
        if(currentPlayer!= null && currentPlayer.isPlaying()){
            notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.ic_pause_grey600_18dp);
            notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, pausePendingIntent);
        }else{
            notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.ic_play_grey600_18dp);
            notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
        }
        notificationRemoteView.setOnClickPendingIntent(R.id.icon_cancel_notification, cancelPendingIntent);
        notificationRemoteView.setTextViewText(R.id.notification_title , notificationTitle);
        notificationRemoteView.setTextViewText(R.id.notification_Message , notificationMessage);
    }

    public void setPlayer(Player player)
    {
        this.currentPlayer = player;
    }

    @Override
    public void onError() {

        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onError();
        }
    }

    @Override
    public void onMediaLagging() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onMediaLagging();
        }

    }

    @Override
    public void onMediaNotSeekable() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onMediaNotSeekable();
        }

    }

    @Override
    public void onPreparing() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onPreparing();
        }

    }

    @Override
    public void onPrepared() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onPrepared();
        }

    }

    @Override
    public void onPlaybackPaused() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onPlaybackPaused();
        }else{
            showPauseNotification();
        }

    }

    @Override
    public void onPlaybackStarted() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onPlaybackStarted();
        }else{
            showResumeNotification();
        }

    }

    @Override
    public void onPlaybackComplete() {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onPlaybackComplete();
        }else{
            showPauseNotification();
        }
    }

    @Override
    public void onFullScreen(boolean isFullScreen) {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.onFullScreen(isFullScreen);
        }

    }

    @Override
    public void callSettings(Point p) {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.callSettings(p);
        }

    }

    @Override
    public void callPlayerSeeked(long lastPostion, long newPosition, boolean isRewindClicked) {
        if(iPlayerListenerActivityCallbacks != null){
            iPlayerListenerActivityCallbacks.callPlayerSeeked(lastPostion, newPosition, isRewindClicked);
        }
    }

    /**
     * This method will return a player according to the fragment with that states of the player
     * If an audio fragment connects first time then this will make a new player and and sava en return it
     * WE NEED THIS METHOD TO MAINTAIN THE STATE OF EACH AUDIO BEING OR BEEN PLAYED SO THAT WE DON'T HAVE TO PREPARE
     * EACH TIME A FRAGEMNT GETS VISIBLE
     * @param tag - unique per fragment
     * @return
     */
//    public IPlayer getPlayer(String tag)
//    {
//        Log.d(TAG , "GET PLAYER CALL");
//        if(connectedPlayers.containsKey(tag)){
//            currentPlayer = connectedPlayers.get(tag);
//            Log.d(TAG , "RETURNING FROM EXISTING PLAYERS");
//
//            return currentPlayer;
//
//        }
//        else{
//            Log.d(TAG , "NO PLAYER EXIST _ ADDING");
//
//            connectedPlayers.put(tag , new Player());
//            currentPlayer = connectedPlayers.get(tag);
//            return currentPlayer;
//        }
//    }

    public Player addPlayer(Player player , String tag)
    {
        Log.d(TAG , "ADD PLAYER CALL");
        connectedPlayers.put(tag , player);
        currentPlayer = connectedPlayers.get(tag);
        return (Player) currentPlayer;
    }

    public Player getOrAddPLayer(Player player , String tag)
    {
        Log.d(TAG , "GET OR ADD PLAYER CALL");
        if(connectedPlayers.containsKey(tag))
        {
            currentPlayer = connectedPlayers.get(tag);
            return (Player) currentPlayer;
        }
        else{
            if(player == null)
            {
                connectedPlayers.put(tag , new Player());
            }else{
                connectedPlayers.put(tag , player);
            }
            currentPlayer = connectedPlayers.get(tag);
            return (Player) currentPlayer;
        }
    }


    /**
     * Inner binder class
     */
    public class MusicBinder extends Binder {
        public AudioMediaService getService()
        {
            return AudioMediaService.this;
        }
    }

    public void setNotificationTitle(String title)
    {
        this.notificationTitle = title;
    }
    public void setNotificationMessage(String message){
        this.notificationMessage = message;
    }

    public void setEnrolledCourse(EnrolledCoursesResponse enrolledCourse)
    {
        this.courseData = enrolledCourse;
    }
    public void setComponentId(String courseComponentId)
    {
        this.courseComponentId = courseComponentId;
    }
}