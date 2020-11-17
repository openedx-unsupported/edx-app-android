package org.edx.mobile.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.inject.Inject;
import com.joanzapata.iconify.widget.IconImageButton;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.googlecast.GoogleCastDelegate;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.DeviceSettingUtil;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.OrientationDetector;
import org.edx.mobile.util.TextUtils;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.util.Version;
import org.edx.mobile.util.VideoUtil;
import org.edx.mobile.view.BaseCourseUnitVideoFragment;
import org.edx.mobile.view.custom.IconProgressBar;
import org.edx.mobile.view.dialog.CCLanguageDialogFragment;
import org.edx.mobile.view.dialog.IListDialogCallback;
import org.edx.mobile.view.dialog.RatingDialogFragment;
import org.edx.mobile.view.dialog.SpeedDialogFragment;

import java.io.Serializable;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;

import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

@SuppressLint("WrongViewCast")
@SuppressWarnings("serial")
public class PlayerFragment extends BaseFragment implements IPlayerListener, Serializable,
        AudioManager.OnAudioFocusChangeListener, NetworkObserver,
        GoogleCastDelegate.CastSessionListener {

    private enum VideoNotPlayMessageType {
        IS_CLEAR, IS_VIDEO_MESSAGE_DISPLAYED, IS_VIDEO_ONLY_ON_WEB,
        IS_NETWORK_MESSAGE_DISPLAYED, IS_SHOWN_WIFI_SETTINGS_MESSAGE
    }

    private static final Logger logger = new Logger(PlayerFragment.class.getName());
    private static final String KEY_VIDEO = "video";
    private static final String KEY_PREPARED = "isPrepared";
    private static final String KEY_AUTOPLAY_DONE = "isAutoPlayDone";
    private static final String KEY_MESSAGE_DISPLAYED = "isMessageDisplayed";
    private static final String KEY_TRANSCRIPT = "transcript";

    private static final int MSG_TYPE_TICK = 2014;
    private static final int DELAY_TIME_MS = 1000;
    private static final int UNFREEZE_DELAY_MS = 300;
    private static final int SUBTITLES_DISPLAY_DELAY_MS = 100;

    @Inject
    IEdxEnvironment environment;

    protected PlayerListener player;
    private boolean isPrepared = false;
    private boolean isAutoPlayDone = false;
    private boolean orientationLocked = false;
    private transient OrientationDetector orientationDetector;
    private transient IPlayerEventCallback callback;
    @Nullable
    private transient TranscriptListener transcriptListener;
    private View.OnClickListener nextListner;
    private View.OnClickListener prevListner;
    private AudioManager audioManager;
    private boolean playOnFocusGain = false;
    private CCLanguageDialogFragment ccFragment;
    private SpeedDialogFragment speedDialogFragment;
    private PopupWindow settingPopup;
    private LinkedHashMap<String, String> langList;
    private TimedTextObject subtitlesObj;
    private TranscriptModel transcript;
    private DownloadEntry videoEntry;
    private Object touchExplorationStateChangeListener;

    private EnumSet<VideoNotPlayMessageType> curMessageTypes =  EnumSet.noneOf(VideoNotPlayMessageType.class);

    private boolean isManualFullscreen = false;
    private long currentPosition = 0;
    private boolean pauseDueToDialog;
    private boolean closedCaptionsEnabled = false;
    private long lastSavedPosition;

    private GoogleCastDelegate googleCastDelegate;
    private RelativeLayout rlRemoteCasting;
    private IconImageButton remotePlayButton;
    private TextView tvCastingVideo;

    private final transient Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_TYPE_TICK) {
                if (callback != null) {
                    if(player!=null && player.isPlaying()) {
                        // mark last current position
                        long pos = player.getCurrentPosition();
                        if (pos > 0 && pos != lastSavedPosition) {
                            lastSavedPosition = pos;
                            callback.saveCurrentPlaybackPosition(pos);
                            logger.debug("Current position saved: " + pos);
                        }
                    }
                }

                // repeat this message after every second
                sendEmptyMessageDelayed(MSG_TYPE_TICK, DELAY_TIME_MS);
            }
        }
    };

    public PlayerFragment() {
        curMessageTypes.clear();
    }

    public void setCallback(IPlayerEventCallback callback) {
        this.callback = callback;
    }

    public void setTranscriptCallback(TranscriptListener listener) {
        this.transcriptListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleCastDelegate = GoogleCastDelegate.getInstance(environment.getAnalyticsRegistry());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_player, null);
        ((IconProgressBar)view.findViewById(R.id.loading_indicator)).setIndeterminateIconColor(getResources().getColor(R.color.white));
        return view;
    }

    /**
     * Restores the saved instance of the player.
     */
    private void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            videoEntry = (DownloadEntry) savedInstanceState.get(KEY_VIDEO);
            isPrepared = savedInstanceState.getBoolean(KEY_PREPARED);
            isAutoPlayDone = savedInstanceState.getBoolean(KEY_AUTOPLAY_DONE);
            transcript = (TranscriptModel) savedInstanceState.get(KEY_TRANSCRIPT);
            if (savedInstanceState.getBoolean(KEY_MESSAGE_DISPLAYED)) {
                showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
            }
        }
        if (player == null)
            player = new VideoPlayer(getActivity());
    }

    private void reAttachPlayEventListener() {
        // set the fullscreen flag to correct value
        if (player != null) {
            boolean isLandscape = isScreenLandscape();
            player.setFullScreen(isLandscape);
            player.setPlayerListener(this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            restore(savedInstanceState);
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

            orientationDetector = new OrientationDetector(getActivity()) {
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
            getView().findViewById(R.id.panel_video_only_on_web).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    final StringBuffer urlStringBuffer = new StringBuffer();
                    if (! videoEntry.url.startsWith("http://") && ! videoEntry.url.startsWith("https://")) {
                        urlStringBuffer.append("http://");
                        urlStringBuffer.append( videoEntry.url);
                    } else {
                        urlStringBuffer.append( videoEntry.url);
                    }
                    BrowserUtil.open(getActivity(),
                            urlStringBuffer.toString());
                }

            });
            rlRemoteCasting = getView().findViewById(R.id.rl_remote_casting);
            remotePlayButton = getView().findViewById(R.id.remote_play_button);
            remotePlayButton.setOnClickListener(view -> playVideoOnRemoteDevice(0, true));
            tvCastingVideo = getView().findViewById(R.id.tv_casting_video);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void allowSensorOrientationIfApplicable() {
        try{
            boolean isRotationOn = DeviceSettingUtil.isDeviceRotationON(getActivity());
            if (isRotationOn) {
                // do UI operations only if the fragment is resumed
                if (orientationDetector.isLandscape()) {
                    if (isScreenLandscape()) {
                        logger.debug("Allowing sensor from landscape rotation");
                        isManualFullscreen = false;
                        allowSensorOrientation();
                    }
                } else if (orientationDetector.isPortrait()) {
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

        try{
            final PlayerView playerView = (PlayerView) getView().findViewById(R.id.player_view);
            if(player!=null){
                player.setPlayerView(playerView);

                // setup the flat if player is fullscreen
                player.setFullScreen(isScreenLandscape());
            }
            if(curMessageTypes.contains(VideoNotPlayMessageType.IS_VIDEO_ONLY_ON_WEB)) {
                showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_ONLY_ON_WEB);
            } if(curMessageTypes.contains(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED)){
                showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
            }else if(curMessageTypes.contains(VideoNotPlayMessageType.IS_NETWORK_MESSAGE_DISPLAYED)){
                showNetworkError();
            } else if(curMessageTypes.contains(VideoNotPlayMessageType.IS_SHOWN_WIFI_SETTINGS_MESSAGE)){
                showWifiSettingsMessage();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            handleOnResume();
        }
        if (isCastingOnRemoteDevice()) {
            updateTranscriptVisibility(View.INVISIBLE);
        }
        setUpCastSessionListener(this);
        // Update the google cast session (e.g video is completed)
        if (googleCastDelegate != null) {
            googleCastDelegate.onStatusUpdated();
        }
    }

    private void setUpCastSessionListener(@Nullable GoogleCastDelegate.CastSessionListener listener) {
        if (getParentFragment() != null && getParentFragment().getUserVisibleHint() &&
                getUserVisibleHint() &&
                googleCastDelegate != null) {
            googleCastDelegate.setSessionListener(listener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            handleOnPause();
        }
        setUpCastSessionListener(null);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed()) {
            if (isVisibleToUser) {
                handleOnResume();
            } else {
                handleOnPause();
            }
            setUpCastSessionListener(isVisibleToUser ? this : null);
        }
    }

    public void handleOnResume() {
        if (isCastingOnRemoteDevice()) {
            return;
        }
        setupController();

        if (curMessageTypes.isEmpty()) {
            // display progress until playback actually starts
            showProgress();
        }

        configureAutoHideControls();

        // start playback after 300 milli seconds, so that it works on HTC One, Nexus5, S4, S5
        // some devices take little time to be ready
        if (isPrepared) {
            handler.postDelayed(unfreezeCallback, UNFREEZE_DELAY_MS);
        }
        rlRemoteCasting.setVisibility(View.GONE);
    }

    private void configureAutoHideControls() {
        player.setAutoHideControls(!getTouchExploreEnabled());
        setTouchExploreChangeListener(new AccessibilityManager.TouchExplorationStateChangeListener() {
            @Override
            public void onTouchExplorationStateChanged(boolean enabled) {
                player.setAutoHideControls(!enabled);
            }
        });
    }

    public void handleOnPause(){
        try{
            orientationDetector.stop();
            handler.removeCallbacks(unfreezeCallback);
            handler.removeCallbacks(requestAccessibilityFocusCallback);
            freezePlayer();
            setTouchExploreChangeListener(null);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        setTouchExploreChangeListener(null);

        if(audioManager!=null) {
            audioManager.abandonAudioFocus(this);
        }
        if(player!=null){
            handler.removeMessages(MSG_TYPE_TICK);
            freezePlayer();
            removeSubtitleCallBack();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            // reset player when user goes back, and there is no state saving happened
            player.reset();

            // release the player instance
            player.release();
            player = null;
            logger.debug("player detached, reset and released");
        }
    }

    private void showProgress() {
        try {
            if(player!=null){
                player.hideController();
            }
            if( this.curMessageTypes.isEmpty() ){
                getView().findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void hideProgress() {
        try {
            getView().findViewById(R.id.loading_indicator).setVisibility(View.GONE);
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        logger.debug("Saving state ...");
        if (player != null) {
            freezePlayer();
        }
        outState.putSerializable(KEY_VIDEO, videoEntry);
        outState.putBoolean(KEY_PREPARED, isPrepared);
        outState.putBoolean(KEY_AUTOPLAY_DONE, isAutoPlayDone);
        //FIXME: ensure that prepare is called on all activity restarts and then this can be removed
        outState.putSerializable(KEY_TRANSCRIPT, transcript);
        super.onSaveInstanceState(outState);
    }

    public synchronized void prepare(DownloadEntry video, TranscriptModel trModel) {
        playOrPrepare(video, trModel, true);
    }

    public synchronized void play(DownloadEntry video, TranscriptModel trModel) {
        playOrPrepare(video, trModel, false);
    }

    /**
     * Starts playing given path. Path can be file path or http/https URL.
     *
     * @param prepareOnly <code>true</code> player will be prepared but not start to play
     */
    public synchronized void playOrPrepare(DownloadEntry video, TranscriptModel trModel, boolean prepareOnly) {
        isPrepared = false;
        // block to portrait while preparing
        if (!isScreenLandscape()) {
            exitFullScreen();
        }

        // reset the player, so that pending play requests will be cancelled
        try {
            player.reset();
        } catch (Exception e) {
            logger.error(e);
        }

        if (video != null) {
            this.videoEntry = video;
        }

        if (trModel != null) {
            this.transcript = trModel;
        }
        this.langList = LocaleUtils.getLanguageList(transcript);
        // request focus on audio channel, as we are starting playback
        requestAudioFocus();
        String path = VideoUtil.getVideoPath(getActivity(), videoEntry);
        try {
            if (videoEntry.isVideoForWebOnly) {
                showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_ONLY_ON_WEB);
                path = "";
            } else {
                // show loading indicator as player will prepare now
                showProgress();

                if (path == null || path.trim().length() == 0) {
                    showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
                    //return;
                } else {
                    hideVideoNotPlayInfo(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
                }
            }

            player.setLMSUrl(videoEntry.lmsUrl);
            player.setVideoTitle(videoEntry.title);

            final long seekTo = videoEntry.getLastPlayedOffset();
            logger.debug("playing [seek=" + seekTo + "]: " + path);
            if (googleCastDelegate.isConnected()) {
                playVideoOnRemoteDevice(lastSavedPosition, true);
                player.setUri(path, seekTo);
            } else if (prepareOnly)
                player.setUri(path, seekTo);
            else
                player.setUriAndPlay(path, seekTo);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void setupController() {
        if (null == player) {
            return;
        }
        try {
            View f = getView();
            if (f == null) {
                return;
            }
            final ViewGroup container = (ViewGroup) f
                    .findViewById(R.id.preview_container);
            final PlayerController controller = new PlayerController(
                    getActivity());
            controller.setAnchorView(container);

            // changed to true after Lou's comments to hide the controllers
            controller.setAutoHide(true);

            controller.setNextPreviousListeners(nextListner, prevListner);
            player.setController(controller);
            reAttachPlayEventListener();

        } catch(Exception e) {
            logger.error(e);
        }
    }

    public void setNextPreviousListeners(View.OnClickListener next, View.OnClickListener prev) {
        this.prevListner = prev;
        this.nextListner = next;
        updateNextPreviousListeners();
    }

    private void updateNextPreviousListeners() {
        if (player != null) {
            if (isScreenLandscape()) {
                player.setNextPreviousListeners(nextListner, prevListner);
            }
            else {
                player.setNextPreviousListeners(null, null);
            }
        }
    }

    @Override
    public void onError() {
        // display error panel
        showNetworkError();

        if (callback != null) {
            callback.onError();
        }

        setScreenOnWhilePlaying(false);
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
        setScreenOnWhilePlaying(true);
        hideNetworkError();
        showProgress();
    }

    @Override
    public void onPlaybackPaused() {
        setScreenOnWhilePlaying(false);

        try{
            if(player!=null){
                double current_time = player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND ;
                environment.getAnalyticsRegistry().trackVideoPause(videoEntry.videoId, current_time,
                        videoEntry.eid, videoEntry.lmsUrl, null);
            }
        }catch(Exception e){
            logger.error(e);
        }
        hideVideoSpeedPopup();
        hideCCPopUp();
        hideSettingsPopUp();
    }

    private void hideNetworkError() {
        try {
            unlockOrientation();
            View errorView = getView().findViewById(R.id.panel_network_error);
            errorView.setVisibility(View.GONE);
            curMessageTypes.remove(VideoNotPlayMessageType.IS_SHOWN_WIFI_SETTINGS_MESSAGE);
            curMessageTypes.remove(VideoNotPlayMessageType.IS_NETWORK_MESSAGE_DISPLAYED);

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
                    if(!curMessageTypes.contains(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED)){
                        //This has been commented after Lou's suggestion
                        unlockOrientation();
                        //lockOrientation();
                        hideVideoSpeedPopup();
                        hideCCPopUp();
                        hideSettingsPopUp();
                        player.hideController();

                        clearAllErrors();
                        // if network is available , this must be video-corrupt-error
                        if (NetworkUtil.isConnected(getActivity())) {
                            // video might be corrupt
                            showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
                        } else {
                            View errorView = getView().findViewById(R.id.panel_network_error);
                            errorView.setVisibility(View.VISIBLE);
                        }

                        curMessageTypes.add(VideoNotPlayMessageType.IS_NETWORK_MESSAGE_DISPLAYED);
                        resetClosedCaptioning();
                    }
                }
            }else{
                if (NetworkUtil.isConnected(getActivity())) {
                    // video might be corrupt
                    showVideoNotAvailable(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
                } else {
                    View errorView = getView().findViewById(R.id.panel_network_error);
                    errorView.setVisibility(View.VISIBLE);
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void showVideoNotAvailable( VideoNotPlayMessageType reason ){
        try {
            if(player!=null){
                hideVideoSpeedPopup();
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

                View errorView;
                if ( reason == VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED)
                    errorView = getView().findViewById(R.id.panel_video_not_available);
                else
                    errorView = getView().findViewById(R.id.panel_video_only_on_web);
                errorView.setVisibility(View.VISIBLE);

                curMessageTypes.add(reason);
                hideClosedCaptioning();
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void hideVideoNotPlayInfo(VideoNotPlayMessageType reason) {
        try {
            View errorView;
            if ( reason == VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED)
                errorView = getView().findViewById(R.id.panel_video_not_available);
            else
                errorView = getView().findViewById(R.id.panel_video_only_on_web);
            errorView.setVisibility(View.GONE);
            curMessageTypes.remove(reason);
        } catch(Exception ex) {
            logger.error(ex);
        }
    }


    private void clearAllErrors() {
        hideNetworkError();
        hideVideoNotPlayInfo(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED);
        hideVideoNotPlayInfo(VideoNotPlayMessageType.IS_VIDEO_ONLY_ON_WEB);
        hideProgress();
    }

    @Override
    public void onPrepared() {
        // mark prepared and allow orientation
        isPrepared = true;

        if (getActivity() == null) {
            return;
        }

        allowSensorOrientation();

        if (!isResumed() || !getUserVisibleHint() || isCastingOnRemoteDevice()) {
            freezePlayer();
            hideProgress();
            return;
        }

        // clear errors
        clearAllErrors();
        handler.postDelayed(unfreezeCallback, UNFREEZE_DELAY_MS);
        environment.getAnalyticsRegistry().trackVideoLoading(videoEntry.videoId, videoEntry.eid,
                videoEntry.lmsUrl);
    }

    @Override
    public void onPlaybackStarted() {
        // mark prepared as playback has started
        isPrepared = true;

        // request audio focus, as playback has started
        requestAudioFocus();

        // keep screen ON
        setScreenOnWhilePlaying(true);

        if (callback != null) {
            callback.onPlaybackStarted();
            updateController("playback started");
        }

        clearAllErrors();
        player.setPlaybackSpeed(loginPrefs.getDefaultPlaybackSpeed());
        if (subtitlesObj != null) {
            showClosedCaptionData(subtitlesObj);
        } else {
            initializeClosedCaptioning();
        }

        try{
            if(player!=null){
                double current_time = player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND ;
                environment.getAnalyticsRegistry().trackVideoPlaying(videoEntry.videoId, current_time
                        , videoEntry.eid, videoEntry.lmsUrl, "");
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void setScreenOnWhilePlaying(boolean screenOn) {
        try {
            if (screenOn) {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                logger.debug("KEEP SCREEN ON is set while playing, flag added");
            }
            else {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                logger.debug("KEEP SCREEN ON is unset, flag removed");
            }
        } catch(Exception ex) {
            logger.error(ex, true);
        }
    }

    @Override
    public void onPlaybackComplete() {
        try{
            if(player!=null){
                double current_time = player.getCurrentPosition()/AppConstants.MILLISECONDS_PER_SECOND ;
                environment.getAnalyticsRegistry().trackVideoStop(videoEntry.videoId,
                        current_time, videoEntry.eid, videoEntry.lmsUrl);
            }
        }catch(Exception e){
            logger.error(e);
        }

        if (callback != null) {
            callback.onPlaybackComplete();
        }
        hideVideoSpeedPopup();
        hideCCPopUp();
        hideSettingsPopUp();
        try{
            if(player!=null){
                if(player.getController()!=null){
                    player.getController().showSpecial( (getTouchExploreEnabled() ? 0L : 5000L) );
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
        if (!environment.getConfig().getAppStoreUris().isEmpty() &&
                environment.getConfig().isAppReviewsEnabled() &&
                NetworkUtil.isConnected(getContext())) {
            final PrefManager.AppInfoPrefManager appPrefs = new PrefManager.AppInfoPrefManager(MainApplication.application);
            final float appRating = appPrefs.getAppRating();
            // If user has not given rating yet, open dialog
            // consider not rated if rating is -1 or less (default is -1)
            if (appRating <= AppConstants.APP_NOT_RATED_THRESHOLD) {
                showRatingDialog();
            } else if (appRating <= AppConstants.APP_NEGATIVE_RATING_THRESHOLD) {
                try {
                    final Version oldVersion = new Version(appPrefs.getLastRatedVersion());
                    final Version curVersion = new Version(BuildConfig.VERSION_NAME);
                    if (oldVersion.isNMinorVersionsDiff(curVersion, AppConstants.MINOR_VERSIONS_DIFF_REQUIRED_FOR_NEGATIVE_RATERS)) {
                        // App updated to 2 minor versions
                        showRatingDialog();
                    }
                } catch (ParseException e) {
                    /** Build version number doesn't correspond to the schema, its a build
                     configuration error **/
                    logger.error(e, true);
                }
            }
        }
    }

    public void showRatingDialog() {
        RatingDialogFragment.newInstance(
                new RatingDialogFragment.OnCancelListener() {
                    @Override
                    public void onCancel() {
                        handler.postDelayed(requestAccessibilityFocusCallback, 2 * UNFREEZE_DELAY_MS);
                    }
                }
        ).show(getFragmentManager(), null);
    }

    @Override
    public void onFullScreen(boolean isFullScreen) {
        if (isPrepared) {
            isManualFullscreen = isFullScreen;
            if (isFullScreen) {
                enterFullScreen();
            } else {
                exitFullScreen();
            }
            environment.getAnalyticsRegistry().trackVideoOrientation(videoEntry.videoId,
                    player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                    isFullScreen, videoEntry.eid, videoEntry.lmsUrl, null);
        } else {
            logger.debug("Player not prepared ?? full screen will NOT work!");
        }
    }

    private void enterFullScreen() {
        try {
            if (getActivity() != null) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if (isPrepared) {
                if (environment.getAnalyticsRegistry() == null) {
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
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void exitFullScreen() {
        try {
            if (getActivity() != null) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            if (isPrepared) {
                if (environment.getAnalyticsRegistry() == null) {
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
                if (player != null) {

                    player.unfreeze();
                    hideProgress();
                    if (player.isPlaying() || getTouchExploreEnabled()) {
                        updateController("player unfreezed");
                    }

                    if (isPrepared && !isAutoPlayDone) {
                        isAutoPlayDone = true;
                        player.start();
                    }

                    if (pauseDueToDialog) {
                        pauseDueToDialog = false;
                        player.pause();
                    }
                }
                orientationDetector.start();
                handler.sendEmptyMessage(MSG_TYPE_TICK);
            }

            handler.postDelayed(requestAccessibilityFocusCallback, UNFREEZE_DELAY_MS);
        }
    };

    /**
     * Request accessibility focus after 300 milli seconds, so that it works on some devices like
     * HTC One, Nexus5, S4, S5 that take some time to be ready.
     */
    private Runnable requestAccessibilityFocusCallback = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                player.requestAccessibilityFocusPausePlay();
            }
        }
    };

    @Override
    public void onOnline() {
        //Nothing to do
    }

    @Override
    public void onOffline() {
        // nothing to do
        showNetworkError();
    }

    public void onConnectedToMobile(){
        boolean wifiPreference = environment.getUserPrefs().isDownloadOverWifiOnly();
        if(!NetworkUtil.isOnZeroRatedNetwork(getActivity(), environment.getConfig()) && wifiPreference){
            //If the user is connected to a non zero rated mobile data network and his wifi preference is on,
            //then prompt user to set change his wifi settings
            showWifiSettingsMessage();
        }else{
            handleNetworkChangeVideoPlayback();
        }
    }

    public void onConnectedToWifi(){
        //Start playing video is user is connected to wifi
        handleNetworkChangeVideoPlayback();
    }

    /**
     * This method handles video playback on network change callbacks
     */
    private void handleNetworkChangeVideoPlayback(){
        hideNetworkError();
        try {
            if(player!=null){
                if(!curMessageTypes.contains(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED)){
                    if((!player.isPaused()
                            && !player.isPlaying() && !player.isPlayingLocally())
                            || (player.isInError() || player.isReset())){
                        showProgress();
                    }
                }
                if (player.isInError() || player.isReset()) {
                    //If player is either in error state or has been reset, restart the player with current position
                    player.restart(currentPosition);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
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

    /**
     * This function sets the closed caption data on the TextView
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
                        subTitlesTv.setText(TextUtils.formatHtml(temp));
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
    private void initializeClosedCaptioning() {
        try {
            removeSubtitleCallBack();
            hideClosedCaptioning();
            if (transcriptListener != null) {
                transcriptListener.downloadTranscript();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * This removes the callbacks and resets the handlers
     */
    private void removeSubtitleCallBack() {
        if(transcriptListener!=null){
            transcriptListener.updateTranscriptCallbackStatus(false);
        }
        hideClosedCaptioning();
        subtitlesObj = null;
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

    @Nullable
    private AccessibilityManager getAccessibilityManager() {
        return (AccessibilityManager)getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    /**
     * @return True if talkback mode is on
     */
    private boolean getTouchExploreEnabled() {
        boolean ret = false;
        AccessibilityManager am = getAccessibilityManager();
        if (am != null && am.isTouchExplorationEnabled()) {
            ret = true;
        }
        return ret;
    }

    /**
     * Sets the current touch explore state change listener and removes the previous one if necessary
     * @param listener Null value unregisters the current listener, non-null unregisters previous one and registers new one
     *                 If the current listener is the same as the previous one, no operation is performed.
     */
    protected void setTouchExploreChangeListener(@Nullable AccessibilityManager.TouchExplorationStateChangeListener listener) {

        // if current touchExplorerStateChangeListener is identical to previous one, no operation is necessary
        if (listener != touchExplorationStateChangeListener) {
            AccessibilityManager am = getAccessibilityManager();
            if (am != null) {

                /* touch explorer state listeners are additive (i.e. adding one doesn't remove the previous one), so we need to be careful
                * and only register one at a time. so, if the one we currently have is valid (non-null), unregister it.
                * If the new one is valid (non-null) register it.
                */
                if (touchExplorationStateChangeListener != null) {
                    am.removeTouchExplorationStateChangeListener((AccessibilityManager.TouchExplorationStateChangeListener) touchExplorationStateChangeListener);
                }

                if (listener != null) {
                    am.addTouchExplorationStateChangeListener(listener);
                }

                touchExplorationStateChangeListener = listener;
            }
        }
    }

    //The method that displays the popup.
    private void showSettingsPopup(final Point p) {
        try{
            if(player!=null){
                player.getController().setAutoHide(!getTouchExploreEnabled());
                Activity context = getActivity();

                float popupHeight =  getResources().getDimension(R.dimen.settings_popup_height);
                float popupWidth =  getResources().getDimension(R.dimen.settings_popup_width);

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
                            player.getController().setAutoHide(!getTouchExploreEnabled());
                        }
                    }
                });

                // Clear the default translucent background
                settingPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Displaying the popup at the specified location, + offsets.
                settingPopup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x-(int)popupWidth, p.y-(int)popupHeight);

                TextView tv_closedCaption = (TextView) layout.findViewById(R.id.tv_closedcaption);
                if ((langList != null) && (langList.size() > 0))
                {
                    tv_closedCaption.setBackgroundResource(R.drawable.white_rounded_selector);
                    tv_closedCaption.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View paramAnonymousView) {
                            showCCFragmentPopup();
                        }
                    });
                }else{
                    tv_closedCaption.setBackgroundResource(R.drawable.grey_roundedbg);
                    tv_closedCaption.setOnClickListener(null);
                }

                layout.findViewById(R.id.tv_video_speed).setOnClickListener(v -> showVideoSpeedFragmentPopup());
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This function is used to show Dialog fragment of
     * Video playback speed list
     */
    private void showVideoSpeedFragmentPopup() {
        hideSettingsPopUp();
        speedDialogFragment = SpeedDialogFragment.getInstance(new SpeedDialogFragment.IListDialogCallback() {
            @Override
            public void onItemClicked(Float speed) {
                if (player != null) {
                    float oldSpeed = loginPrefs.getDefaultPlaybackSpeed();
                    loginPrefs.saveDefaultPlaybackSpeed(speed);
                    player.setPlaybackSpeed(speed);

                    environment.getAnalyticsRegistry().trackVideoSpeed(videoEntry.videoId,
                            player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                            videoEntry.eid, videoEntry.lmsUrl, oldSpeed, speed);
                }
            }

            @Override
            public void onCancelClicked() {
                speedDialogFragment.dismiss();
            }
        }, loginPrefs.getDefaultPlaybackSpeed());
        speedDialogFragment.show(getFragmentManager(), "video_speed_dialog");
        speedDialogFragment.setCancelable(true);
    }

    /**
     *This function is used to show Dialog fragment of
     *language list in potrait mode
     */
    protected void showCCFragmentPopup() {
        try{
            hideSettingsPopUp();
            langList = LocaleUtils.getLanguageList(transcript);
            ccFragment = CCLanguageDialogFragment.getInstance(langList, new IListDialogCallback() {
                @Override
                public void onItemClicked(HashMap<String, String> lang) {
                    closedCaptionsEnabled = true;
                    final String languageSubtitle = lang.keySet().toArray()[0].toString();
                    setSubtitleLanguage(languageSubtitle);
                    if (player != null) {
                        environment.getAnalyticsRegistry().trackShowTranscript(videoEntry.videoId,
                                player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                                videoEntry.eid, videoEntry.lmsUrl);
                        environment.getAnalyticsRegistry().trackTranscriptLanguage(videoEntry.videoId,
                                player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                                languageSubtitle, videoEntry.eid, videoEntry.lmsUrl);
                    }
                    if (transcriptListener != null) {
                        transcriptListener.downloadTranscript();
                    }
                    if (player != null) {
                        player.getController().setSettingsBtnDrawable(false);
                        player.getController().setAutoHide(true);
                    }
                }

                @Override
                public void onCancelClicked() {
                    closedCaptionsEnabled = false;
                    hideClosedCaptioning();
                    setSubtitleLanguage(getString(R.string.lbl_cc_none));
                    if (player != null) {
                        environment.getAnalyticsRegistry().trackHideTranscript(videoEntry.videoId,
                                player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                                videoEntry.eid, videoEntry.lmsUrl);
                    }
                    if (player != null) {
                        player.getController().setAutoHide(true);
                        player.getController().setSettingsBtnDrawable(false);
                    }
                }
            }, getSubtitleLanguage());

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
     * This function hides the Video Speed popup and overlay
     */
    private void hideVideoSpeedPopup() {
        if (speedDialogFragment != null && speedDialogFragment.isVisible()) {
            speedDialogFragment.dismiss();
        }
    }

    /**
     * This function hides the CC popup and overlay
     */
    private void hideCCPopUp() {
        if (ccFragment != null && ccFragment.isVisible()) {
            ccFragment.dismiss();
        }
    }

    @Inject
    LoginPrefs loginPrefs;

    @Nullable
    private String getSubtitleLanguage() {
        return loginPrefs.getSubtitleLanguage();
    }

    private void setSubtitleLanguage(@NonNull String language) {
        loginPrefs.setSubtitleLanguage(language);
    }

    @Override
    public void callPlayerSeeked(long lastPostion, long newPosition, boolean isRewindClicked) {
        try{
            if (callback != null) {
                // mark last seeked position
                callback.saveCurrentPlaybackPosition((int) newPosition);
                logger.debug("Current position saved: " + newPosition);
            }

            if(isRewindClicked){
                resetClosedCaptioning();
            }
            environment.getAnalyticsRegistry().trackVideoSeek(videoEntry.videoId,
                    lastPostion/AppConstants.MILLISECONDS_PER_SECOND,
                    newPosition/AppConstants.MILLISECONDS_PER_SECOND,
                    videoEntry.eid, videoEntry.lmsUrl,
                    isRewindClicked);
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * Displays controller
     *
     * @param source The source which called this function
     */
    private void updateController(String source) {
        logger.debug("Updating controller from : " + source);

        if (player != null) {
            // controller should also refresh, so hide and show it
            player.hideController();
            player.showController();
            updateNextPreviousListeners();
        }
    }

    /**
     * @return the video model that this fragment is supposed to play.
     */
    public VideoModel getPlayingVideo() {
        return videoEntry;
    }

    /**
     * @return true if playback is ongoing, false otherwise.
     */
    public boolean isPlaying() {
        return (player != null && player.isPlaying());
    }

    /**
     * Returns true if video player is in frozen state
     *
     * @return <code>true</code> if the video player is frozen
     */
    @SuppressWarnings("unused")
    public boolean isFrozen() {
        return (player != null && player.isFrozen());
    }

    public void freezePlayer() {
        setScreenOnWhilePlaying(false);

        if (player!=null) {
            if (callback != null && player.isPlaying()) {
                long pos = player.getCurrentPosition();
                if (pos > 0) {
                    callback.saveCurrentPlaybackPosition(pos);
                }
            }

            player.freeze();
        }
    }

    /**
     * This method is called when we need to notify the user during playback that he has
     * switched to mobile network and his current download settings is not allowed to download videos
     */
    private void showWifiSettingsMessage() {
        try {
            if (player != null) {
                if (player.isPlayingLocally()) {
                    hideNetworkError();
                } else {
                    if(!curMessageTypes.contains(VideoNotPlayMessageType.IS_VIDEO_MESSAGE_DISPLAYED) && !player.isInError()){
                        unlockOrientation();
                        hideVideoSpeedPopup();
                        hideCCPopUp();
                        hideSettingsPopUp();
                        if ( !player.isReset()) {
                            if ( !player.isInError()) {
                                currentPosition = player.getCurrentPosition();
                            }
                            player.reset();
                        }
                        player.hideController();

                        clearAllErrors();
                        View errorView = getView().findViewById(R.id.panel_network_error);
                        errorView.setVisibility(View.VISIBLE);
                        TextView errorHeaderTextView = (TextView) errorView.findViewById(R.id.error_header);
                        errorHeaderTextView.setText(getString(R.string.wifi_off_message));
                        errorView.findViewById(R.id.error_message).setVisibility(View.GONE);
                        curMessageTypes.add(VideoNotPlayMessageType.IS_SHOWN_WIFI_SETTINGS_MESSAGE);
                    }
                    resetClosedCaptioning();
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * @return true if message is displayed on player to change wifi settings.
     */
    public boolean isShownWifiSettingsMessage(){
        return curMessageTypes.contains(VideoNotPlayMessageType.IS_SHOWN_WIFI_SETTINGS_MESSAGE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        player.setFullScreen(isLandscape);
        updateController("orientation change");
        player.requestAccessibilityFocusPausePlay();
    }

    /**
     * Utility for seeking the video player to specified {@link Caption}.
     *
     * @param caption The caption to seek to.
     */
    public void seekToCaption(@NonNull Caption caption) {
        if (isCastingOnRemoteDevice()) {
            return;
        }
        player.seekTo(caption.start.getMseconds());
    }

    public void playVideoOnRemoteDevice(long lastSavedPosition, boolean autoPlay) {
        if (googleCastDelegate != null && videoEntry != null) {
            googleCastDelegate.loadRemoteMedia(getActivity(), videoEntry, lastSavedPosition, autoPlay);
            rlRemoteCasting.setVisibility(View.VISIBLE);
            remotePlayButton.setVisibility(View.GONE);
            updateTranscriptVisibility(View.INVISIBLE);
        }
    }

    private void updateTranscriptVisibility(int visibility) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof BaseCourseUnitVideoFragment) {
            ((BaseCourseUnitVideoFragment) fragment).updateBottomSectionVisibility(visibility);
        }
    }

    @Override
    public void onApplicationConnected() {
        playVideoOnRemoteDevice(lastSavedPosition, true);
        handleOnPause();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onApplicationDisconnected() {
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
            if (!isPrepared && videoEntry != null) {
                prepare(videoEntry, transcript);
            }
            handleOnResume();
            updateTranscriptVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVideoComplete() {
        remotePlayButton.setVisibility(View.VISIBLE);
        tvCastingVideo.setVisibility(View.GONE);
    }

    @Override
    public void onVideoPlaying() {
        remotePlayButton.setVisibility(View.GONE);
        tvCastingVideo.setVisibility(View.VISIBLE);
    }

    public boolean isCastingOnRemoteDevice() {
        return googleCastDelegate != null && googleCastDelegate.isConnected();
    }

    /**
     * This Method used to get the current position of the player
    * */
    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    /**
     * This method is used to check either player is initialized, and can process subtitles on the basis of
     * current status of player
     */
    public boolean canProcessSubtitles() {
        return player != null && (player.isPlaying() || player.isPaused());
    }

    /**
     * This method is used to update the CC data when a transcript is selected
     */
    public void updateClosedCaptionData(Caption subtitle) {
        setClosedCaptionData(closedCaptionsEnabled ? subtitle : null);
    }

    /**
     * This method is used to display CC data when a transcript is selected
     */
    public void showClosedCaptionData(TimedTextObject subtitles) {
        this.subtitlesObj = subtitles;
        resetClosedCaptioning();
        if (subtitlesObj != null) {
            closedCaptionsEnabled = true;
            if (player != null) {
                environment.getAnalyticsRegistry().trackShowTranscript(videoEntry.videoId,
                        player.getCurrentPosition() / AppConstants.MILLISECONDS_PER_SECOND,
                        videoEntry.eid, videoEntry.lmsUrl);
            }
        }
        if(transcriptListener!=null){
            transcriptListener.updateTranscriptCallbackStatus(true);
        }
    }
}
