package org.edx.mobile.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.DeviceSettingUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.OrientationDetector;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.ClosedCaptionAdapter;
import org.edx.mobile.view.dialog.CCLanguageDialogFragment;
import org.edx.mobile.view.dialog.IListDialogCallback;
import org.edx.mobile.view.dialog.InstallFacebookDialog;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import subtitleFile.Caption;
import subtitleFile.FormatSRT;
import subtitleFile.TimedTextObject;

@SuppressLint("WrongViewCast")
@SuppressWarnings("serial")
public class PlayerFragment extends Fragment implements IPlayerListener, Serializable, AudioManager.OnAudioFocusChangeListener, PlayerController.ShareVideoListener {

    private static final int MSG_TYPE_TICK = 2014;
    private static final int DELAY_TIME = 1000;
    protected IPlayer player;
    private boolean isPrepared = false;
    private boolean stateSaved = false;
    private boolean orientationLocked = false;
    private transient OrientationDetector orientation;
    private transient IPlayerEventCallback callback;
    private View.OnClickListener nextListner;
    private View.OnClickListener prevListner;
    private AudioManager audioManager;
    private boolean playOnFocusGain = false;
    private Handler subtitleDisplayHandler = new Handler();
    private Handler subtitleFetchHandler = new Handler();
    private CCLanguageDialogFragment ccFragment;
    private PopupWindow settingPopup;
    private PopupWindow cc_popup;
    private LinkedHashMap<String, TimedTextObject> srtList;
    private LinkedHashMap<String, String> langList;
    private TimedTextObject srt;
    private String languageSubtitle;
    private LayoutInflater layoutInflater;
    private TranscriptModel transcript;
    private DownloadEntry videoEntry;
    private ISegment segIO;
    private boolean isVideoMessageDisplayed;
    private boolean isNetworkMessageDisplayed;
    private boolean isManualFullscreen = false;

    private final Logger logger = new Logger(getClass().getName());

    private IUiLifecycleHelper uiHelper;
    private boolean pauseDueToDialog;

    private final transient Handler handler = new Handler() {
        private int lastSavedPosition;
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_TYPE_TICK) {
                if (callback != null) {
                    if(player!=null){
                        // mark last current position
                        int pos = player.getCurrentPosition();
                        if (pos > 0 && pos != lastSavedPosition) {
                            lastSavedPosition = pos;
                            callback.saveCurrentPlaybackPosition(pos);
                            logger.debug("Current position saved: " + pos);
                        }
                    }
                }

                // repeat this message after every second
                sendEmptyMessageDelayed(MSG_TYPE_TICK, DELAY_TIME);
            }
        }
    };

    public PlayerFragment() {
        isVideoMessageDisplayed = false;
        isNetworkMessageDisplayed = false;
    }

    public void setCallback(IPlayerEventCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onVideoShare() {

        if (this.videoEntry == null || TextUtils.isEmpty(this.videoEntry.getYoutubeVideoUrl())) {return;}

        FacebookProvider fbProvider = new FacebookProvider();
        FacebookDialog dialog = (FacebookDialog) fbProvider.shareVideo(getActivity(), this.videoEntry.getTitle(), this.videoEntry.getYoutubeVideoUrl());
        if (dialog != null) {
            uiHelper.trackPendingDialogCall(dialog.present());
            pauseDueToDialog = true;
        } else {
            new InstallFacebookDialog().show(getFragmentManager(), null);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        segIO = SegmentFactory.getInstance();
        // save this fragment across activity re-creations
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_player, null);
        this.layoutInflater = inflater;

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {

            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {

            }
        });

    }

    /**
     * Restores the saved instance of the player.
     * 
     * @param savedInstanceState
     */
    private void restore(Bundle savedInstanceState) {
        try{
            if (player == null) {
                if (savedInstanceState != null
                        && savedInstanceState.containsKey("player")) {
                    player = (IPlayer) savedInstanceState.get("player");
                } else {
                    player = new Player();
                }
            }
            if (savedInstanceState != null
                    && savedInstanceState.containsKey("isMessageDisplayed")){
                if(savedInstanceState.getBoolean("isMessageDisplayed")){
                    showVideoNotAvailable();
                }
            }
            reAttachPlayEventListener();
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void reAttachPlayEventListener() {
        // set the fullscreen flag to correct value
        if (player != null) {
            boolean isLandscape = isScreenLandscape();
            player.setFullScreen(isLandscape);
            player.setPlayerListener(this);
            player.setShareVideoListener(this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            restore(savedInstanceState);
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

            orientation = new OrientationDetector(getActivity()) {
                private boolean isLastRotationOn = false;
                @Override
                protected void onChanged() {
                    if (isResumed()) {
                        allowSensorOrientationIfApplicable();
                    }
                }
                
                @Override
                protected void onUpdate() {
                    super.onUpdate();
                    boolean isRotationOn = DeviceSettingUtil.isDeviceRotationON(getActivity());
                    if ( !isRotationOn && isLastRotationOn) {
                        // rotation just got turned OFF, so exit fullscreen
                        exitFullScreen();
                    }
                    isLastRotationOn = isRotationOn;
                }
            };
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void allowSensorOrientationIfApplicable() {
        try{
            boolean isRotationOn = DeviceSettingUtil.isDeviceRotationON(getActivity());
            if (isRotationOn) {
                // do UI operations only if the fragment is resumed
                if (orientation.isLandscape()) {
                    if (isScreenLandscape()) {
                        logger.debug("Allowing sensor from landscape rotation");
                        isManualFullscreen = false;
                        allowSensorOrientation();
                    }
                } else if (orientation.isPortrait()) {
                    if ( !isScreenLandscape()) {
                        logger.debug("Allowing sensor from portrait rotation");
                        isManualFullscreen = false;
                        allowSensorOrientation();
                    }
                }
            } else {
                logger.debug("Locking to portrait as Device Screen Rotation is OFF");
                // lock to portrait
                if ( !isManualFullscreen) {
                    exitFullScreen();
                } else {
                    logger.debug("You are in manual fullscreen mode");
                }
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        logger.debug("Player fragment start");
        
        stateSaved = false;
        try{
            Preview preview = (Preview) getView().findViewById(R.id.preview);
            if(player!=null){
                player.setPreview(preview);

                // setup the flat if player is fullscreen
                player.setFullScreen(isScreenLandscape());
            }
            if(isVideoMessageDisplayed){
                showVideoNotAvailable();
            }else if(isNetworkMessageDisplayed){
                showNetworkError();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        uiHelper.onResume();

        setupController();

        if(!isNetworkMessageDisplayed && !isVideoMessageDisplayed){
            // display progress until playback actually starts
            showProgress();
        }

        // start playback after 300 milli seconds, so that it works on HTC One, Nexus5, S4, S5
        // some devices take little time to be ready
        handler.postDelayed(unfreezeCallback, 300);
    }

    @Override
    public void onPause() {
        super.onPause();

        uiHelper.onPause();

        try{
            orientation.stop();
            handler.removeCallbacks(unfreezeCallback);
            if(player!=null){
                player.freeze();
                if (callback != null) {
                    // mark last freeze position
                    callback.saveCurrentPlaybackPosition(player.getLastFreezePosition());
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    } 

    @Override
    public void onStop() {
        super.onStop();
        if(audioManager!=null) {
            audioManager.abandonAudioFocus(this);
        }
        if(player!=null){
            player.freeze();
            handler.removeMessages(MSG_TYPE_TICK);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();

        if (!stateSaved) {
            if (player!=null) {
                // reset player when user goes back, and there is no state saving happened
                player.reset();
                removeSubtitleCallBack();

                // release the player instance
                player.release();
                player = null;
                logger.debug("player detached, reset and released");
            }
        }
    }

    private void showProgress() {
        try {
            if(player!=null){
                player.hideController();
            }
            if(!isNetworkMessageDisplayed && !isVideoMessageDisplayed){
                getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void hideProgress() {
        try {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        logger.debug("Saving state ...");
        stateSaved = true;
        if(player!=null){
            // hold on until activity is being destroyed, otherwise we assume next call would be restart()
            boolean changingConfig = getActivity().isChangingConfigurations();
            logger.debug("Player fragment changing config?  =" + changingConfig);
            if ( !changingConfig) {
                // you MUST PAUSE the video  
                // only if screen is stopping due to any reason other than CONFIGURATION CHANGE
                player.setPausedOnUnfreeze();
            }
            
            player.freeze();
            outState.putSerializable("player", player);
        }
        super.onSaveInstanceState(outState);

        uiHelper.onSaveInstanceState(outState);

    }

    /**
     * Starts playing given path. Path can be file path or http/https URL.
     * 
     * @param path
     * @param seekTo
     * @param title
     */
    public synchronized void play(String path, int seekTo, String title, 
            TranscriptModel trModel, DownloadEntry video) {
        isPrepared = false;
        // block to portrait while preparing
        if ( !isScreenLandscape()) {
            exitFullScreen();
        }

        // clear all errors
        clearAllErrors();

        // reset the player, so that pending play requests will be cancelled
        try {
            player.reset();
        } catch (Exception e) {
            logger.error(e);
        }

        if(video!=null){
            this.videoEntry = video;
        }

        if (trModel != null)
        {
            this.transcript = trModel;
            TranscriptManager tm = new TranscriptManager(getActivity());
            tm.downloadTranscriptsForVideo(trModel);
            //initializeClosedCaptioning();
        }

        try{
            PrefManager pm = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
            languageSubtitle = pm.getString(PrefManager.Key.TRANSCRIPT_LANGUAGE);
        }catch(Exception e){
            logger.error(e);
        }

        // request focus on audio channel, as we are starting playback
        requestAudioFocus();

        try {
            // show loading indicator as player will prepare now
            showProgress();

            if (path == null || path.trim().length() == 0) {
                showVideoNotAvailable();
                //return;
            } else {
                hideVideoNotAvailable();
            }
            this.transcript = trModel;
            player.setLMSUrl(video.lmsUrl);
            player.setVideoTitle(title);

            logger.debug("playing [seek=" + seekTo + "]: " + path);

            boolean enableShare = videoEntry != null && !TextUtils.isEmpty(videoEntry.getYoutubeVideoUrl()) && new FacebookProvider().isLoggedIn();

            player.setShareEnabled(enableShare);

            player.setUriAndPlay(path, seekTo);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void setupController() {
        try{
            if (player != null) {
                View f = getView();
                if (f != null) {
                    ViewGroup container = (ViewGroup) f
                            .findViewById(R.id.preview_container);
                    PlayerController controller = new PlayerController(
                            getActivity());
                    controller.setAnchorView(container);

                    // changed to true after Lou's comments to hide the controllers
                    controller.setAutoHide(true);

                    controller.setPrevNextListeners(nextListner, prevListner);
                    player.setController(controller);
                    player.setShareVideoListener(this);
                    //
                    reAttachPlayEventListener();
                    
                } else {
                    //error("failed to set controller, view is NULL")
                }
            } else {
                //error("failed to set controller, player is NULL")
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void setPrevNxtListners(View.OnClickListener next, View.OnClickListener prev) {
        if (player != null) {
            this.prevListner = prev;
            this.nextListner = next;
            player.setNextPreviousListener(next, prev);
        }

    }

    @Override
    public void onError() {
        // display error panel
        showNetworkError();

        if (callback != null) {
            callback.onError();
        }
    }

    @Override
    public void onVideoLagging() {
        if ( !NetworkUtil.isConnected(getActivity())) {
            // no network and video lagging, might be network problem
            showNetworkError();
        }
    }

    @Override
    public void onVideoNotSeekable() {
    }

    @Override
    public void onPreparing() {
        hideNetworkError();
        showProgress();
    }

    @Override
    public void onPlaybackPaused() {
        try{
            if(player!=null){
                double current_time = player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND ;
                segIO.trackVideoPause(videoEntry.videoId, current_time,
                        videoEntry.eid, videoEntry.lmsUrl);
            }
        }catch(Exception e){
            logger.error(e);
        }

        hideCCPopUp();
        hideSettingsPopUp();
    }

    private void hideNetworkError() {
        try {
            unlockOrientation();
            View errorView = getView().findViewById(R.id.panel_network_error);
            errorView.setVisibility(View.GONE);
            isNetworkMessageDisplayed = false;
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void showNetworkError() {
        try {
            if(player!=null){
                if (player.isPlayingLocally() || player.isPlaying() ) {
                    hideNetworkError();
                } else {
                    if(!isVideoMessageDisplayed){
                        //This has been commented after Lou's suggestion
                        unlockOrientation();
                        //lockOrientation();
                        hideCCPopUp();
                        hideSettingsPopUp();
                        player.hideController();

                        clearAllErrors();
                        // if network is available , this must be video-corrupt-error
                        if (NetworkUtil.isConnected(getActivity())) {
                            // video might be corrupt
                            showVideoNotAvailable();
                        } else {
                            View errorView = getView().findViewById(R.id.panel_network_error);
                            errorView.setVisibility(View.VISIBLE);
                        }

                        isNetworkMessageDisplayed = true;
                        resetClosedCaptioning();
                    }
                }
            }else{
                if (NetworkUtil.isConnected(getActivity())) {
                    // video might be corrupt
                    showVideoNotAvailable();
                } else {
                    View errorView = getView().findViewById(R.id.panel_network_error);
                    errorView.setVisibility(View.VISIBLE);
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void showVideoNotAvailable() {
        try {
            if(player!=null){
                hideCCPopUp();
                hideSettingsPopUp();
                player.hideController();
                // player got error, 
                // mark player as prepared, because it is not in preparing state anymore
                isPrepared = true;

                //This has been put after Lou's Suggestion
                unlockOrientation();
                //lockOrientation();

                hideProgress();

                View errorView = getView().findViewById(R.id.panel_video_not_available);
                errorView.setVisibility(View.VISIBLE);

                isVideoMessageDisplayed = true;
                hideClosedCaptioning();
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void hideVideoNotAvailable() {
        try {
            View errorView = getView().findViewById(R.id.panel_video_not_available);
            errorView.setVisibility(View.GONE);
            isVideoMessageDisplayed = false;
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void clearAllErrors() {
        hideNetworkError();
        hideVideoNotAvailable();
        hideProgress();
    }

    @Override
    public void onPrepared() {
        if ( !isResumed() 
                || !isVisible()) {
            if(player!=null){
                player.freeze();
            }
            return;
        }

        // clear errors
        clearAllErrors();

        try{
            segIO.trackVideoLoading(videoEntry.videoId, videoEntry.eid, 
                    videoEntry.lmsUrl);
        }catch(Exception e){
            logger.error(e);
        }

        // mark prepared and allow orientation
        isPrepared = true;
        initializeClosedCaptioning();
        allowSensorOrientation();
    }

    @Override
    public void onPlaybackStarted() {
        // mark prepared as playback has started
        isPrepared = true;

        // request audio focus, as playback has started
        requestAudioFocus();

        if (callback != null) {
            callback.onPlaybackStarted();
            updateController("playback started");
        }

        clearAllErrors();
        if(langList!=null){
            displaySrtData();
        }else{
            initializeClosedCaptioning();
        }

        try{
            if(player!=null){
                double current_time = player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND ;
                segIO.trackVideoPlaying(videoEntry.videoId, current_time
                        , videoEntry.eid, videoEntry.lmsUrl);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onPlaybackComplete() {
        try{
            if(player!=null){
                double current_time = player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND ;
                segIO.trackVideoStop(videoEntry.videoId,
                        current_time, videoEntry.eid, videoEntry.lmsUrl);
            }
        }catch(Exception e){
            logger.error(e);
        }

        if (callback != null) {
            // mark offset as zero, so that playback will resume from start next time
            callback.saveCurrentPlaybackPosition(0);
            callback.onPlaybackComplete();
        }
        //removeSubtitleCallBack();
        hideCCPopUp();
        hideSettingsPopUp();
        try{
            if(player!=null){
                if(player.getController()!=null){
                    player.getController().show(5000);
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void callLMSServer(String url) {
        try{
            if(url!=null){
                BrowserUtil.open(getActivity(), url);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onFullScreen(boolean isFullScreen) {
        if (isPrepared) {
            // stop orientation updates before locking the screen
            orientation.stop();
            if(player!=null){
                player.freeze();
            }

            isManualFullscreen = isFullScreen;
            if (isFullScreen) {
                enterFullScreen();
            } else {
                exitFullScreen();
            }
        } else {
            logger.debug("Player not prepared ?? full screnn will NOT work!");
        }
    }

    protected void showLandscape() {
        try{
            if(player!=null){
                player.freeze();

                Intent i = new Intent(getActivity(), LandscapePlayerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("player", player);
                startActivity(i);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void enterFullScreen() {
        try {
            getActivity().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (isPrepared) {
                if (segIO == null) {
                    logger.warn("segment is NOT initialized, cannot capture event enterFullScreen");
                    return;
                }
                if (player == null) {
                    logger.warn("player instance is null, cannot capture event enterFullScreen");
                    return;
                }
                if (videoEntry == null) {
                    logger.warn("video model instance is null, cannot capture event enterFullScreen");
                    return;
                }

                segIO.trackVideoOrientation(videoEntry.videoId,
                        player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                        true, videoEntry.eid, videoEntry.lmsUrl);
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void exitFullScreen() {
        try {
            getActivity().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (isPrepared) {
                if (segIO == null) {
                    logger.warn("segment is NOT initialized, cannot capture event exitFullScreen");
                    return;
                }
                if (player == null) {
                    logger.warn("player instance is null, cannot capture event exitFullScreen");
                    return;
                }
                if (videoEntry == null) {
                    logger.warn("video model instance is null, cannot capture event exitFullScreen");
                    return;
                }

                segIO.trackVideoOrientation(videoEntry.videoId,
                        player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                        false, videoEntry.eid, videoEntry.lmsUrl);
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void allowSensorOrientation() {
        if (isPrepared && !orientationLocked) {
            getActivity().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    private boolean isScreenLandscape() {
        try {
            int orientation = getResources().getConfiguration().orientation;
            logger.debug("Current orientation = " + orientation);
            return (orientation == Configuration.ORIENTATION_LANDSCAPE);
        } catch(Exception ex) {
            logger.error(ex);
        }
        return false;
    }

    private Runnable unfreezeCallback = new Runnable() {

        @Override
        public void run() {
            if (isResumed() && !isRemoving()) {
                hideProgress();
                if(player!=null) {
                    player.unfreeze();
                    if (player.isPlaying()) {
                        hideProgress();
                        updateController("player unfreezed");
                    }

                    if (pauseDueToDialog){
                        pauseDueToDialog = false;
                        player.pause();
                    }

                }
                orientation.start();
                handler.sendEmptyMessage(MSG_TYPE_TICK);
            }
        }
    };

    public void onOnline() {
        hideNetworkError();
        try {
            if(player!=null){
                if(!isVideoMessageDisplayed){
                    if(!player.isPaused() 
                            && !player.isPlaying() && !player.isPlayingLocally()){
                        showProgress();
                    }
                }
                if (player.isInError()) {
                    player.restart();
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void onOffline() {
        // nothing to do
        showNetworkError();
    }

    /**
     * LectureComplete dialog was creating problems if orientation is allowed when dialog is shown.
     * So, locked orientation while the LectureComplete dialog is showing.
     */
    public void lockOrientation() {
        orientationLocked = true;
        if (isScreenLandscape()) {
            enterFullScreen();
        } else {
            exitFullScreen();
        }
    }

    public void unlockOrientation() {
        orientationLocked = false;
        allowSensorOrientationIfApplicable();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        try {
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (playOnFocusGain) {
                    // before we start playing, request focus on audio channel
                    requestAudioFocus();
                    if(player!=null){
                        player.start();
                        updateController("audio focus gained");
                    }
                }
                playOnFocusGain = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if(player!=null){
                    // resume playback
                    if (player.isPlaying()) {
                        player.pause();
                        updateController("audio focus lost");
                        playOnFocusGain = true;
                    } else {
                        playOnFocusGain = false;
                    }
                }
                break;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void requestAudioFocus() {
        if(audioManager!=null) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }


    public void handleSettings(Point p) {

    }

    /**
     * This runnable handles the displaying of 
     * Subtitles on the screen per 100 mili seconds
     */
    private Runnable subtitleProcessesor = new Runnable() {
        @Override
        public void run() {
            try{
                //This has been reset so that previous cc will not be displayed
                resetClosedCaptioning();
                if (player != null && (player.isPlaying() || player.isPaused())) {
                    int currentPos = player.getCurrentPosition();
                    if(srt!=null){
                        Collection<Caption> subtitles = srt.captions.values();
                        for (Caption caption : subtitles) {
                            if (currentPos >= caption.start.mseconds
                                    && currentPos <= caption.end.mseconds) {
                                setClosedCaptionData(caption);
                                break;
                            } else if (currentPos > caption.end.mseconds) {
                                setClosedCaptionData(null);
                            }
                        }
                    }else{
                        setClosedCaptionData(null);
                    }
                }
                subtitleDisplayHandler.postDelayed(this, 100);
            }catch(Exception e){
                logger.error(e);
            }
        }
    };

    /**
     * This runnable is used the fetch the Subtitle in TimedTextObject
     */
    private Runnable subtitleFetchProcessesor = new Runnable()
    {
        public void run()
        {
            if(srtList!=null){
                srtList = null;
            }

            srtList = new LinkedHashMap<String, TimedTextObject>();
            try
            {
                TranscriptManager tm = new TranscriptManager(getActivity());
                LinkedHashMap<String, InputStream> localHashMap = tm
                        .fetchTranscriptsForVideo(transcript,getActivity());
                
                if (localHashMap != null){
                    Object[] keyList = localHashMap.keySet().toArray();
                    for(int i=0; i<keyList.length; i++){
                        InputStream localInputStream = (InputStream)localHashMap.get(keyList[i]);
                        if (localInputStream != null)
                        {
                            TimedTextObject localTimedTextObject =
                                    new FormatSRT().parseFile("temp.srt", localInputStream);
                            srtList.put(keyList[i].toString(), localTimedTextObject);
                            //srtList.add(localTimedTextObject);
                            if (localInputStream != null)
                                localInputStream.close();
                        }
                    }

                    if ((srtList == null) || (srtList.size() == 0)) {
                        subtitleFetchHandler.postDelayed(subtitleFetchProcessesor, 100);
                    }else{
                        displaySrtData();
                    }
                }else{
                    subtitleFetchHandler.postDelayed(subtitleFetchProcessesor, DELAY_TIME);
                }  
            }catch (Exception localException) {
                logger.error(localException);
            }

        }
    };

    /**
     * Handler initialized for fetching Subtitles 
     */
    private void fetchSubtitlesTask(){
        try
        {
            if (this.subtitleFetchHandler != null)
            {
                subtitleFetchHandler.removeCallbacks(this.subtitleFetchProcessesor);
                subtitleFetchHandler = null;
            }
            LinkedHashMap<String, String> languageList = getLanguageList();
            if(languageList!=null && languageList.size()>0){
                subtitleFetchHandler = new Handler();
                if (subtitleFetchProcessesor != null)
                    subtitleFetchHandler.post(this.subtitleFetchProcessesor);
            }
        } catch (Exception localException) {
            logger.error(localException);
        }
    }


    /**
     * This function sets the closed caption data on the TextView
     * @param text
     */
    private void setClosedCaptionData(Caption text){
        try{
            RelativeLayout subTitlesLayout = (RelativeLayout) getActivity().findViewById(R.id.txtSubtitles);
            TextView subTitlesTv = (TextView) getActivity().findViewById(R.id.txtSubtitles_tv);
            if(subTitlesTv!=null ){
                if(text!=null){
                    int margin_twenty_dp = (int) UiUtil.getParamsInDP(getResources(),20);
                    int margin_ten_dp = (int) UiUtil.getParamsInDP(getResources(),10);
                    if(player!=null){
                        LayoutParams lp = (LayoutParams) subTitlesLayout.getLayoutParams();
                        if (player.getController()!=null && player.getController().isShown()){
                            if(player.isFullScreen()){
                                lp.setMargins(margin_twenty_dp, 0,
                                        margin_twenty_dp, (int)UiUtil.getParamsInDP(getResources(),50));
                            }else{
                                lp.setMargins(margin_twenty_dp, 0,
                                        margin_twenty_dp,(int)UiUtil.getParamsInDP(getResources(),42));
                            }
                            subTitlesLayout.setLayoutParams(lp);
                        }else{
                            if(player.isFullScreen()){
                                lp.setMargins(margin_twenty_dp, 0,
                                        margin_twenty_dp, margin_ten_dp);
                            }else{
                                lp.setMargins(margin_twenty_dp, 0,
                                        margin_twenty_dp,(int)UiUtil.getParamsInDP(getResources(),5));
                            }
                            subTitlesLayout.setLayoutParams(lp);
                        }
                    }
                    subTitlesTv.setPadding(margin_ten_dp, (int)UiUtil.getParamsInDP(getResources(),2),
                            margin_ten_dp,(int)UiUtil.getParamsInDP(getResources(),2) );
                    
                    subTitlesTv.setText("");
                    //This has been done because text.content contains <br />
                    //in the end of each message
                    String temp = text.content;
                    if(temp.endsWith("<br />")){
                        temp = temp.substring(0, temp.length()-6);
                    }
                    if(temp.length()==0){
                        subTitlesTv.setVisibility(View.GONE);
                    }else{
                        subTitlesTv.setText(Html.fromHtml(temp));
                        subTitlesTv.setVisibility(View.VISIBLE);
                    }
                }else{
                    subTitlesTv.setVisibility(View.GONE);
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * Hide the Closed Captioning TextView
     */
    private void hideClosedCaptioning(){
        try{
            TextView subTitlesTv = (TextView) getActivity().findViewById(R.id.txtSubtitles_tv);
            if(subTitlesTv!=null){
                subTitlesTv.setVisibility(View.GONE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This resets the Closed Captioning to blank/empty
     */
    private void resetClosedCaptioning(){
        try{
            TextView subTitlesTv = (TextView) getActivity().findViewById(R.id.txtSubtitles_tv);
            if(subTitlesTv!=null){
                subTitlesTv.setText("");
                subTitlesTv.setVisibility(View.INVISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * Initialiaze and reset Closed Captioning handlers
     */
    private void initializeClosedCaptioning(){
        try{
            removeSubtitleCallBack();
            hideClosedCaptioning();
            fetchSubtitlesTask();
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This removes the callbacks and resets the handlers
     */
    private void removeSubtitleCallBack() {

        if (subtitleDisplayHandler != null)
        {
            subtitleDisplayHandler.removeCallbacks(subtitleProcessesor);
            subtitleDisplayHandler = null;
            hideClosedCaptioning();
            srt = null;
            srtList = null;
            //subtitleSelected = -1;
            //languageSubtitle = getString(R.string.lbl_cc_cancel);
        }
        if (subtitleFetchHandler != null)
        {
            subtitleFetchHandler.removeCallbacks(subtitleFetchProcessesor);
            subtitleFetchHandler = null;
        }
    }

    /**
     * This removes the subtitle display callback
     */
    private void removeSubtitleDisplayCallBack() {

        if (subtitleDisplayHandler != null)
        {
            subtitleDisplayHandler.removeCallbacks(subtitleProcessesor);
            subtitleDisplayHandler = null;
            hideClosedCaptioning();
            //subtitleSelected = -1;
            languageSubtitle = getString(R.string.lbl_cc_cancel);
        }
    }

    @Override
    public void callSettings(Point p) {
        try{
            ImageView iv = (ImageView) getActivity().findViewById(R.id.iv_transparent_bg);
            iv.setVisibility(View.VISIBLE);
        }catch(Exception e){
            logger.error(e);
        }
        showSettingsPopup(p);
    }

    public void hideTransparentImage() {
        try{
            ImageView iv = (ImageView) getActivity().findViewById(R.id.iv_transparent_bg);
            iv.setVisibility(View.GONE);
        }catch(Exception e){
            logger.error(e);
        }
    }

    //The method that displays the popup.
    private void showSettingsPopup(final Point p) {
        try{
            if(player!=null){
                player.getController().setAutoHide(false);
                Activity context = getActivity();
                Resources r = getResources();
                float popupHeight = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 40 , r.getDisplayMetrics());

                float popupWidth = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 150 , r.getDisplayMetrics());

                // Inflate the popup_layout.xml
                LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.setting_popup);
                LayoutInflater layoutInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.panel_settings_popup, viewGroup);

                // Creating the PopupWindow
                settingPopup = new PopupWindow(context);
                settingPopup.setContentView(layout);
                settingPopup.setWidth((int)popupWidth);
                settingPopup.setHeight((int)popupHeight);
                settingPopup.setFocusable(true);
                settingPopup.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        hideTransparentImage();
                        if(player!=null){
                            player.getController().setSettingsBtnDrawable(false);
                            player.getController().setAutoHide(true);
                        }
                    }
                });

                // Clear the default translucent background
                settingPopup.setBackgroundDrawable(new BitmapDrawable());

                // Displaying the popup at the specified location, + offsets.
                settingPopup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x-(int)popupWidth, p.y-(int)popupHeight);

                TextView tv_closedCaption = (TextView) layout.findViewById(R.id.tv_closedcaption);
                if ((langList != null) && (langList.size() > 0))
                {
                    tv_closedCaption.setBackgroundResource(R.drawable.white_rounded_selector);
                    tv_closedCaption.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View paramAnonymousView)
                        {
                            if(player.isFullScreen()) {
                                showClosedCaptionLandscapePopup(p);
                            }else{
                                showCCFragmentPopup();
                            }
                        }
                    });
                    return;
                }else{
                    tv_closedCaption.setBackgroundResource(R.drawable.grey_roundedbg);
                    tv_closedCaption.setOnClickListener(null);
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This function is used to show popup in landscape mode and 
     * the Point defines the current position of Settings button
     * @param {@link android.graphics.Point}
     */
    private void showClosedCaptionLandscapePopup(Point p){
        try{
            LinkedHashMap<String, String> languageList = getLanguageList();
            float popupHeight = UiUtil.getParamsInDP(getResources(),220);
            float popupWidth = UiUtil.getParamsInDP(getResources(),250);

            // Inflate the popup_layout.xml
            LinearLayout viewGroup = (LinearLayout) getActivity()
                    .findViewById(R.id.cc_layout_popup);

            View layout = layoutInflater.inflate(R.layout.panel_cc_popup, viewGroup);

            // Creating the PopupWindow for CC
            cc_popup = new PopupWindow(getActivity());
            cc_popup.setContentView(layout);
            cc_popup.setWidth((int)popupWidth);
            cc_popup.setHeight((int)popupHeight);
            cc_popup.setFocusable(true);
            cc_popup.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    hideSettingsPopUp();
                }
            });

            ListView lv_ccLang = (ListView) layout.findViewById(R.id.cc_list);
            ClosedCaptionAdapter ccAdaptor = new
                    ClosedCaptionAdapter(getActivity()) {
                @Override
                public void onItemClicked(HashMap<String, String> lang) {
                    try{
                        languageSubtitle = lang.keySet().toArray()[0].toString();
                        try{
                            PrefManager pm = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                            pm.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, languageSubtitle);
                        }catch(Exception e){
                            logger.error(e);
                        }
                        //subtitleSelected = pos;
                        try{
                            if(player!=null){
                                segIO.trackTranscriptLanguage(videoEntry.videoId,
                                        player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND,
                                        languageSubtitle , videoEntry.eid, videoEntry.lmsUrl);
                            }
                        }catch(Exception e){
                            logger.error(e);
                        }

                        displaySrtData();
                        cc_popup.dismiss();
                        if(player!=null){
                            player.getController().setSettingsBtnDrawable(false);
                            player.getController().setAutoHide(true);
                        }
                    }catch(Exception e){
                        logger.error(e);
                    }
                }
            };
            lv_ccLang.setAdapter(ccAdaptor);
            lv_ccLang.setOnItemClickListener(ccAdaptor);

            //LinkedHashMap<String, String> languageList = getLanguageList();
            if(languageList!=null && languageList.size()>0){
                HashMap<String, String> lang;
                for(int i=0; i<languageList.size();i++){
                    lang = new HashMap<String, String>();
                    lang.put(languageList.keySet().toArray()[i].toString(), 
                            languageList.values().toArray()[i].toString());
                    ccAdaptor.add(lang);
                }
            }
            ccAdaptor.selectedLanguage = languageSubtitle;
            ccAdaptor.notifyDataSetChanged();

            // Clear the default translucent background
            cc_popup.setBackgroundDrawable(new BitmapDrawable());

            // Displaying the popup at the specified location, + offsets.
            cc_popup.showAtLocation(layout, Gravity.NO_GRAVITY, 
                    p.x + 10 -(int)popupWidth, p.y + 10 - (int)popupHeight);

            TextView tv_none = (TextView) layout.findViewById(R.id.tv_cc_cancel);
            if(languageSubtitle!=null){
                if(languageSubtitle.equalsIgnoreCase("none")){
                    tv_none.setBackgroundResource(R.color.cyan_text_navigation_20);
                }else{
                    tv_none.setBackgroundResource(R.drawable.white_bottom_rounded_selector);
                }
            }else{
                tv_none.setBackgroundResource(R.color.cyan_text_navigation_20);
            }
            tv_none.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        removeSubtitleDisplayCallBack();
                        hideCCPopUp();

                        try{
                            PrefManager pm = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                            pm.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, getString(R.string.lbl_cc_cancel));
                        }catch(Exception e){
                            logger.error(e);
                        }
                        try{
                            if(player!=null){
                                segIO.trackHideTranscript(videoEntry.videoId,
                                        player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND,
                                        videoEntry.eid, videoEntry.lmsUrl);
                            }
                        }catch(Exception e){
                            logger.error(e);
                        }

                        if(player!=null){
                            player.getController().setSettingsBtnDrawable(false);
                            player.getController().setAutoHide(true);
                        }
                    }catch(Exception e){
                        logger.error(e);
                    }
                }
            });
        }catch(Exception e){
            logger.error(e);
        }
    }

    //Default Language List
    private LinkedHashMap<String, String> getLanguageList(){
        if(transcript!=null){
            langList = transcript.getLanguageList(getActivity());
            return langList;
        }
        return null;
    }

    // 

    /**
     *This function is used to show Dialog fragment of 
     *language list in potrait mode   
     */
    protected void showCCFragmentPopup() {
        try{
            hideSettingsPopUp();

            ccFragment = CCLanguageDialogFragment.getInstance(getLanguageList(),new IListDialogCallback() {
                @Override
                public void onItemClicked(HashMap<String, String> lang) {
                    try{
                        languageSubtitle = lang.keySet().toArray()[0].toString();
                        try{
                            PrefManager pm = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                            pm.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, languageSubtitle);
                            if(player!=null){
                                segIO.trackShowTranscript(videoEntry.videoId,
                                        player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND,
                                        videoEntry.eid, videoEntry.lmsUrl);
                                segIO.trackTranscriptLanguage(videoEntry.videoId,
                                        player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND,
                                        languageSubtitle , videoEntry.eid, videoEntry.lmsUrl);
                            }
                        }catch(Exception e){
                            logger.error(e);
                        }
                        displaySrtData();
                        if(player!=null){
                            player.getController().setSettingsBtnDrawable(false);
                            player.getController().setAutoHide(true);
                        }
                    }catch(Exception e){
                        logger.error(e);
                    }
                }

                @Override
                public void onCancelClicked() {
                    try{
                        removeSubtitleDisplayCallBack();
                        try{
                            PrefManager pm = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                            pm.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, getString(R.string.lbl_cc_cancel));
                            if(player!=null){
                                segIO.trackHideTranscript(videoEntry.videoId,
                                        player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND,
                                        videoEntry.eid, videoEntry.lmsUrl);
                            }
                        }catch(Exception e){
                            logger.error(e);
                        }
                        if(player!=null){
                            player.getController().setAutoHide(true);
                            player.getController().setSettingsBtnDrawable(false);
                        }
                    }catch(Exception e){
                        logger.error(e);
                    }

                }
            }, languageSubtitle);

            ccFragment.setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog);
            ccFragment.show(getFragmentManager(), "dialog");
            ccFragment.setCancelable(true);
        }catch(Exception e){
            logger.error(e);
        }
    }


    /**
     * This function hides the Settings popup and overlay 
     */
    private void hideSettingsPopUp(){
        try{
            hideTransparentImage();
            if(settingPopup!=null){
                settingPopup.dismiss();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This function hides the CC popup and overlay 
     */
    private void hideCCPopUp(){
        try{
            if(cc_popup!=null){
                cc_popup.dismiss();
            }
            if(ccFragment!=null && ccFragment.isVisible()){
                ccFragment.dismiss();
            }

        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This function is used to display CC data when a transcript is selected  
     */
    private void displaySrtData(){
        try{
            if (subtitleDisplayHandler != null) {
                subtitleDisplayHandler.removeCallbacks(subtitleProcessesor);
            }
            resetClosedCaptioning();
            if(srtList!=null && srtList.size()>0){
                if(languageSubtitle!=null){
                    if(!languageSubtitle.equalsIgnoreCase(getString(R.string.lbl_cc_cancel))){
                        srt = srtList.get(languageSubtitle);
                        if (srt != null) {
                            try{
                                if(player!=null){
                                    segIO.trackShowTranscript(videoEntry.videoId,
                                            player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND,
                                            videoEntry.eid, videoEntry.lmsUrl);
                                }
                            }catch(Exception e){
                                logger.error(e);
                            }
                            if(subtitleDisplayHandler==null){
                                subtitleDisplayHandler = new Handler();
                            }
                            subtitleDisplayHandler.post(subtitleProcessesor);
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void callPlayerSeeked(long lastPostion, long newPosition, boolean isRewindClicked) {
        try{
            if(isRewindClicked){
                resetClosedCaptioning();
            }
            segIO.trackVideoSeek(videoEntry.videoId,
                    lastPostion/AppConstants.MILLISECONDS_PER_SECOND,
                    newPosition/AppConstants.MILLISECONDS_PER_SECOND,
                    videoEntry.eid, videoEntry.lmsUrl);
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * Displays controller is in PORTRAIT MODE, otherwise hides controller.
     */
    private void updateController(String source) {
        logger.debug("Updating controller from : " + source);
        
        if (player != null) {
            // controller should also refresh, so hide and show it
            player.hideController();
            
            // if this is LANDSCAPE mode, then let controller be HIDDEN by default
            if (player.isFullScreen()) {
                logger.debug("Player controller hidden because in LANDSCAPE mode");
                
                // by some reason, player is still showing controller may be from some other thread ?
                // so hide controller after a delay
                // FIXME: this should be permanently resolved
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (player != null) {
                            player.hideController();
                        }
                    }
                }, 50 * DELAY_TIME);
            } else {
                player.showController();
                logger.debug("Player controller shown because in PORTRAIT mode");
            }
        }
    }
}