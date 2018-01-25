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
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.view.CourseUnitNavigationActivity;
import org.edx.mobile.view.Router;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by arslan on 1/9/18.
 */

public class AudioMediaService extends Service implements IPlayerListener{

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
    String notificationTitle, notificationMessage;
    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;
    public static final String ACTION_MAIN_ACTIVITY = "MAIN_ACTIVITY";
    public static final String START_SERVICE = "START_SERVICE";
    public static final String PLAY_MEDIA = "PLAY_MEDIA";
    public static final String PAUSE_MEDIA = "PAUSE_MEDIA";
    public static final String DELETE_INTENT = "DELETE_INTENT";
    public static final String CANCEL_INTENT = "CANCEL";
    public static int NOTIFICATION_ID = 101;
    IPlayer currentPlayer;
    HashMap<String , Player> connectedPlayers= new HashMap<>();
    private DownloadEntry audioEntry;
    private static final Logger logger = new Logger(AudioMediaService.class.getName());

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeIntents();
        if (intent != null)
        {
            String action = intent.getAction() == null ? "" : intent.getAction();
            switch (action){
                case START_SERVICE:{
                    //Stop the notification if already shown
                    stopForegroundService(true);
                    break;
                }
                //Play button clicked in notification
                case PLAY_MEDIA:{
                    handleResumeAction();
                    break;
                }
                //Pause button clicked in notification
                case PAUSE_MEDIA:
                {
                    handlePauseAction();
                    break;
                }
                //Cancel button clicked in notification

                case CANCEL_INTENT:
                {
                    resetAllPlayers();
                    releaseAllPlayers();
                    currentPlayer = null;
                    connectedPlayers.clear();
                    stopForegroundService(true);
                    stopSelf();
                    break;
                }
                case DELETE_INTENT:
                {
                    if(!isBound)
                    {
                        handleDeleteCommandUnbound();
                    }
                    else{
                        handleDeleteCommandBound();
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**This will create a notification in notification bar with current music play state**/
    public void startForegroundService()
    {
        if(currentPlayer != null){
            currentPlayer.setPlayerListener(this);
            initializeCustomNotification();
            startForeground(NOTIFICATION_ID,notification);
        }else{
            stopSelf();
        }
    }
    /**This will cancel/hide the notification in notification bar**/
    public void stopForegroundService(boolean shouldRemove)
    {
        if(currentPlayer != null){
            currentPlayer.setPlayerListener(null);
            currentPlayer.reset();
            currentPlayer = null;
        }
        stopForeground(shouldRemove);
        isNotificationShowing = false;
        isRunningForeground = false;
    }
    /**This will update the notification with resume state/pause button**/
    public void showResumeNotification()
    {
        notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.icon_pause_media);
        notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, pausePendingIntent);
        notificationBuilder.setCustomContentView(notificationRemoteView);
        notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID , notification);
        isNotificationShowing = true;
    }

    /**This will update the notification with play state/play button**/
    public void showPauseNotification()
    {
        notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.icon_play_media);
        notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
        notificationBuilder.setCustomContentView(notificationRemoteView);
        notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID , notification);
        isNotificationShowing = true;
    }

    /**
     *
     * Handles the pause button click even clicked through notification or Activity
     */
    private void handlePauseAction()
    {
        showPauseNotification();
        currentPlayer.pause();
        try {
            if (currentPlayer != null && currentPlayer.isPlaying()) {
                    int pos = currentPlayer.getCurrentPosition();
                    if (pos > 0) {
                        saveCurrentPlaybackPosition(pos);
                    }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     *
     * Handles the resume/play button click even clicked through notification or Activity
     */
    private void handleResumeAction()
    {
        showResumeNotification();
        currentPlayer.start();
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

    /**
     * Create the bundle to be passed back into the activity with its states when clicking on the notification.
     * @return
     */
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
        notification = notificationBuilder.setContentTitle("")
                .setSmallIcon(R.drawable.launch_screen_logo)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 100, 70, false))
                .setContentIntent(notificationPendingIntent)
                .setCustomContentView(notificationRemoteView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setDeleteIntent(deletePendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MAX)
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
            notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.icon_pause_media);
            notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, pausePendingIntent);
        }else{
            notificationRemoteView.setImageViewResource(R.id.play_pause, R.drawable.icon_play_media);
            notificationRemoteView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
        }
        notificationRemoteView.setOnClickPendingIntent(R.id.icon_cancel_notification, cancelPendingIntent);
        notificationRemoteView.setTextViewText(R.id.notification_title , notificationTitle);
        notificationRemoteView.setTextViewText(R.id.notification_Message , notificationMessage);
    }

    /**
     * Setting current playing player with its state
     * @param player
     */
    public void setPlayer(Player player)
    {
        this.currentPlayer = player;
    }

    @Override
    public void onError() {

    }

    @Override
    public void onMediaLagging() {

    }

    @Override
    public void onMediaNotSeekable() {

    }

    @Override
    public void onPreparing() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onPlaybackPaused() {
    }

    @Override
    public void onPlaybackStarted() {
    }

    @Override
    public void onPlaybackComplete() {
        showPauseNotification();
        saveCurrentPlaybackPosition(0);
        setPlaybackCompleteStatusInDb();
    }

    @Override
    public void onFullScreen(boolean isFullScreen) {
    }

    @Override
    public void callSettings(Point p) {
    }

    @Override
    public void callPlayerSeeked(long lastPostion, long newPosition, boolean isRewindClicked) {
    }

    /**
     * This method will return a player according to the fragment with that states of the player
     * If an audio fragment connects first time then this will make a new player and and sava en return it
     * WE NEED THIS METHOD TO MAINTAIN THE STATE OF EACH AUDIO BEING OR BEEN PLAYED SO THAT WE DON'T HAVE TO PREPARE
     * EACH TIME A FRAGMENT GETS VISIBLE
     * @param tag - unique per fragment
     * @return
     */

    public Player getOrAddPLayer(Player player , String tag)
    {
        if(connectedPlayers.containsKey(tag))
        {
            currentPlayer = connectedPlayers.get(tag);
            if(player!= null && currentPlayer == player){
                return (Player) currentPlayer;
            }else{
                currentPlayer.reset();
                currentPlayer.release();
                currentPlayer = null;
                connectedPlayers.remove(tag);
                connectedPlayers.put(tag, player);
                currentPlayer = connectedPlayers.get(tag);
                return (Player) currentPlayer;
            }
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
     * Inner binder class to get the service object on Audio Player Fragment
     */
    public class MusicBinder extends Binder {
        public AudioMediaService getService()
        {
            return AudioMediaService.this;
        }
    }

    /**
     * Set the title to be shown on notification - Its actual value is Audio Unit's title
     * @param title
     */
    public void setNotificationTitle(String title)
    {
        this.notificationTitle = title;
    }
    /**
     * Set the title to be shown on notification - Its actual value is Audio Unit's Section Name
     * @param message
     */

    public void setNotificationMessage(String message){
        this.notificationMessage = message;
    }

    /**
     * Set the enrolled course while moving from audio fragment to service so that we can use it to retail the state when clicking the notification
     * Required by the audio fragment holder activity - Receives it in bundle
     * @param enrolledCourse
     */
    public void setEnrolledCourse(EnrolledCoursesResponse enrolledCourse)
    {
        this.courseData = enrolledCourse;
    }

    /**
     * Set the enrolled course component while moving from audio fragment to service so that we can use it to retail the state when clicking the notification
     * Required by the audio fragment holder activity - Receives it in bundle
     * @param courseComponentId
     */
    public void setComponentId(String courseComponentId)
    {
        this.courseComponentId = courseComponentId;
        initializeIntents();
    }

    /**
     * Reset all the players associated to the service when no more audio background is need and ending service
     */
    private void resetAllPlayers()
    {
        for (Player player : connectedPlayers.values()){
            if(player!= null && !player.isReset()){
                player.reset();
            }
        }
    }
    /**
     * Release all the players associated to the service when no more audio background is need and ending service
     */

    private void releaseAllPlayers()
    {
        for (Player player : connectedPlayers.values()){
            if(player!= null){
                player.release();
            }
        }
    }

    /**
     * Handles the action DELETE for the service when service is unbound
     */
    private void handleDeleteCommandUnbound(){
        if(!currentPlayer.isReset()){
            currentPlayer.reset();
        }
        currentPlayer.release();
        currentPlayer = null;
        connectedPlayers.clear();
        stopForegroundService(true);
        stopSelf();
    }
    /**
     * Handles the action DELETE for the service when service is bound
     */

    private void handleDeleteCommandBound()
    {
        stopForegroundService(true);
        isNotificationShowing = false;
    }

    /**
     * Set the current playing player with state when moving to background service
     * @param player
     */
    public void updateCurrentPlayer(IPlayer player)
    {
        currentPlayer = null;
        currentPlayer = player;
    }
    /**
     * Set the current playing Audio to update its playback statuses in the database
     * @param audioEntry
     */

    public void setAudioEntry(DownloadEntry audioEntry)
    {
        this.audioEntry = audioEntry;
    }
    /**
     * Set the current playing Audio playback position in the database - To be retained when audio is played next time
     * @param offset
     */

    private void saveCurrentPlaybackPosition(int offset) {
        try {
            if (audioEntry != null) {
                // mark this as partially watches, as playing has started
                DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE).updateMediaLastPlayedOffset(audioEntry.blockId, offset,
                        null);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void setPlaybackCompleteStatusInDb()
    {
        try {
            if (audioEntry != null && audioEntry.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                audioEntry.watched = DownloadEntry.WatchedState.WATCHED;
                // mark this as partially watches, as playing has started
                DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE)
                        .updatePlayableMediaWatchedState(audioEntry.blockId, DownloadEntry.WatchedState.WATCHED,
                                null);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

    }

}