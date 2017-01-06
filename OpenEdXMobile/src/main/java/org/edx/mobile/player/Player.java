package org.edx.mobile.player;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View.OnClickListener;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.OnSwipeListener;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

@SuppressWarnings("serial")
public class Player extends MediaPlayer implements OnErrorListener,
        OnPreparedListener, OnBufferingUpdateListener,
        OnCompletionListener, OnInfoListener, IPlayer {

    // Player states
    public enum PlayerState {
        RESET, URI_SET, PREPARED,
        PLAYING, PAUSED, ERROR, LAGGING, PLAYBACK_COMPLETE, STOPPED
    }

    private PlayerState state;
    private int bufferPercent;
    private boolean isSeekable;
    private boolean isFullScreen;
    private boolean isPlayingLocally;
    private boolean playWhenPrepared;
    private boolean autoHideControls;
    private transient IPlayerListener callback;
    private transient PlayerController controller;
    private int lastCurrentPosition;
    private int lastFreezePosition;
    private int lastDuration;
    private boolean isFrozen;
    private PlayerState freezeState;
    private int seekToWhenPrepared;
    private String videoTitle;
    private String lmsURL;
    private String videoUri;
    private static final Logger logger = new Logger(Player.class.getName());

    public Player() {
        init();

        setOnErrorListener(this);
        setOnPreparedListener(this);
        setOnBufferingUpdateListener(this);
        setOnCompletionListener(this);
        setLooping(true);
        setOnInfoListener(this);
    }

    /**
     * Resets all the fields of this player.
     */
    private void init() {
        state = PlayerState.RESET;
        bufferPercent = 0;
        isSeekable = true;
        isFullScreen = false;
        isPlayingLocally = true;
        playWhenPrepared = false;
        lastCurrentPosition = 0;
        lastDuration = 0;
        isFrozen = false;
        autoHideControls = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // sometimes, error also causes onCompletion() call
        // avoid on completion if player got an error
        if (state != PlayerState.ERROR) {
            state = PlayerState.PLAYBACK_COMPLETE;
            if (callback != null) {
                callback.onPlaybackComplete();
            }
            seekTo(0);
            logger.debug("Playback complete");
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferPercent = percent;
    }

    @Override
    public int getBufferPercentage() {
        return bufferPercent;
    }

    @Override
    public boolean canPause() {
        if (state == PlayerState.PLAYING
                || state == PlayerState.PREPARED
                || state == PlayerState.PAUSED
                || state == PlayerState.PLAYBACK_COMPLETE
                || state == PlayerState.LAGGING) {
            logger.debug("Can pause = TRUE");
            return true;
        }
        logger.debug("Can pause = FALSE");
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        if ((state == PlayerState.PLAYING
                || state == PlayerState.PREPARED
                || state == PlayerState.STOPPED
                || state == PlayerState.PAUSED
                || state == PlayerState.LAGGING)
                && isSeekable) {
            logger.debug("Can seek back = TRUE");
            return true;
        }
        logger.debug("Can seek back = FALSE");
        return false;
    }

    @Override
    public boolean canSeekForward() {
        if ((state == PlayerState.PLAYING
                || state == PlayerState.PREPARED
                || state == PlayerState.STOPPED
                || state == PlayerState.PAUSED
                || state == PlayerState.LAGGING)
                && isSeekable) {
            logger.debug("Can seek forward = TRUE");
            return true;
        }
        logger.debug("Can seek forward = FALSE");
        return false;
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    @Override
    public void toggleFullScreen() {
        setFullScreen(!isFullScreen);

        if (callback != null) {
            callback.onFullScreen(isFullScreen);
        }
    }

    @Override
    public void callSettings(Point p) {
        if (callback != null) {
            callback.callSettings(p);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        state = PlayerState.PREPARED;
        if (callback != null) {
            callback.onPrepared();
        }

        if (seekToWhenPrepared >= 0) {
            seekTo(seekToWhenPrepared);
        }

        if (playWhenPrepared) {
            start();
            state = PlayerState.PLAYING;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            isSeekable = false;
            if (callback != null) {
                callback.onVideoNotSeekable();
            }
            logger.debug("Track not seekable");
        } else if (what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
            logger.debug("Metadata update received");
        } else if (what == MediaPlayer.MEDIA_INFO_UNKNOWN) {
            logger.debug("Unknown info");
        } else if (what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
            state = PlayerState.LAGGING;
            if (callback != null) {
                callback.onVideoLagging();
            }
            logger.debug("Video track lagging");
        }
        logger.debug("INFO: what=" + what + ";extra=" + extra);
        return true;
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        if (lastCurrentPosition != 0) {
            seekToWhenPrepared = lastCurrentPosition;
        }
        state = PlayerState.ERROR;
        if (callback != null) {
            callback.onError();
        }

        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            logger.warn("ERROR: unknown");
        } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            logger.warn("ERROR: server died");
        } else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            logger.warn("ERROR: video container invalid for progressive playback");
        }
        logger.warn("ERROR: what=" + what + ";extra=" + extra);

        // return TRUE here, so that onCompletionListener will NOT be called
        return true;
    }

    @Override
    public void setUri(String uri, int seekTo) throws Exception {
        load(uri, seekTo, false);
    }

    @Override
    public void setUriAndPlay(String uri, int seekTo) throws Exception {
        load(uri, seekTo, true);
    }

    @Override
    public void restart(int seekTo) throws Exception {
        logger.debug("RestartFreezePosition=" + seekTo);
        lastCurrentPosition = 0;
        // if seekTo=lastCurrentPosition then seekTo() method will not work
        load(videoUri, seekTo, playWhenPrepared);
    }

    @Override
    public void restart() throws Exception {
        restart(seekToWhenPrepared);
    }

    private void load(String videoUri, int seekTo, boolean playWhenPrepared) throws Exception {
        this.videoUri = videoUri;
        this.seekToWhenPrepared = seekTo;
        this.playWhenPrepared = playWhenPrepared;
        this.isFrozen = false;
        this.lastCurrentPosition = 0; // reset last seek position

        // re-display controller, so that it shows latest data
        if (this.controller != null) {
            this.controller.hide();
        }

        reset();
        state = PlayerState.RESET;
        bufferPercent = 0;

        setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (videoUri != null) {
            if (videoUri.startsWith("http")) {
                // this is web URL
                setDataSource(videoUri);

                state = PlayerState.URI_SET;
                isPlayingLocally = false;
            } else {
                // this is file path
                FileInputStream fs = new FileInputStream(new File(videoUri));
                setDataSource(fs.getFD());
                fs.close();

                state = PlayerState.URI_SET;
                isPlayingLocally = true;
            }

            prepareAsync();
            // notify that the player is now preparing
            if (callback != null) {
                callback.onPreparing();
            }
        }
    }

    @Override
    public boolean isPlayingLocally() {
        return isPlayingLocally;
    }

    @Override
    public boolean isInError() {
        return state == PlayerState.ERROR;
    }

    @Override
    public boolean isReset() {
        return state == PlayerState.RESET;
    }

    @Override
    public void setFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;

        if (controller != null) {
            controller.setTopBarVisibility(isFullScreen);
        }
    }

    @Override
    public void setPreview(final Preview preview) {
        if (preview == null) {
            return;
        }
        preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
                                                  int height) {
                try {
                    logger.debug("Player state=" + state);
                    Surface surface = new Surface(surfaceTexture);
                    setSurface(surface);

                    // Keep screen ON while playing
                    // if using SurfaceHolder, just call setScreenOnWhilePlaying(true);
                    // When not using SurfaceHolder, need to use
                    // WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

                    logger.debug("Surface created and set to the player");

                    // preview last shown frame if not playing
                    if (!isPlaying()) {
                        seekTo(lastCurrentPosition);
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        preview.setOnTouchListener(new OnSwipeListener(preview.getContext()) {

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                if (controller != null) {
                    controller.playNext();
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                if (controller != null) {
                    controller.playPrevious();
                }
            }

            @Override
            public void onClick() {
                super.onClick();

                if (controller != null
                        && state != PlayerState.RESET
                        && state != PlayerState.URI_SET) {
                    logger.debug("Player touched");
                    if (controller.isShowing() && autoHideControls) {
                        controller.hide();
                    } else {
                        controller.setLmsUrl(lmsURL);
                        controller.setTitle(videoTitle);
                        controller.show();
                    }
                }
            }
        });
    }

    @Override
    public void setPlayerListener(IPlayerListener listener) {
        this.callback = listener;
    }

    @Override
    public void setController(PlayerController cont) {
        // handle old controller object first
        if (controller == null && this.controller != null) {
            this.controller.setMediaPlayer(null);
        }

        if (this.controller != null) {
            // hide old controller while setting new
            this.controller.hide();
            this.controller = null;
        }

        // now handle new controller object
        this.controller = cont;

        if (this.controller != null) {
            this.controller.setMediaPlayer(this);
            logger.debug("Controller set");
        }
    }

    @Override
    /**
     * Controls will be hidden immediately if changed to false
     */
    public void setAutoHideControls(boolean autoHide) {
        autoHideControls = autoHide;
        if (!autoHide && autoHideControls) {
            hideController();
        }
    }

    @Override
    public boolean getAutoHideControls() {
        return autoHideControls;
    }

    @Override
    public void showController() {
        if (controller != null) {
            controller.hide();
            controller.setTitle(videoTitle);
            controller.setLmsUrl(lmsURL);

            if (autoHideControls) {
                controller.resetShowTimeoutMS();
            }
            else {
                controller.setShowTimeoutMS(0);
            }

            controller.show();

            logger.debug("Player controller shown");
        }
    }

    @Override
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    @Override
    public void requestAccessibilityFocusPausePlay() {
        if (controller != null && controller.isShowing()) {
            controller.requestAccessibilityFocusPausePlay();
        }
    }

    @Override
    public void freeze() {
        if (isFrozen) {
            // keep this method one-shot
            return;
        }

        isFrozen = true;
        freezeState = state;

        setPreview(null);
        setController(null);
        if (isPlaying()) {
            pause();
        }
        if (state == PlayerState.URI_SET && playWhenPrepared) {
            playWhenPrepared = false;
        }
        lastCurrentPosition = getCurrentPosition();
        lastFreezePosition = lastCurrentPosition;
        if (lastCurrentPosition > 0) {
            seekToWhenPrepared = lastCurrentPosition;
        }
        logger.debug("FreezePosition=" + lastFreezePosition);
    }

    @Override
    public void unfreeze() {
        if (isFrozen) {
            logger.debug("unFreezePosition=" + lastFreezePosition);
            lastCurrentPosition = getCurrentPosition();
            seekTo(lastFreezePosition);
            if (freezeState == PlayerState.PLAYING
                    || freezeState == PlayerState.LAGGING) {
                start();
            } else if (freezeState == PlayerState.URI_SET) {
                if (state == PlayerState.PREPARED) {
                    // start playing as player is already prepared
                    start();
                } else {
                    playWhenPrepared = true;
                    if (callback != null) {
                        callback.onPreparing();
                    }
                }
            } else if (freezeState == PlayerState.PAUSED) {
                pause();
            }
        }

        isFrozen = false;
    }

    @Override
    public void setVideoTitle(String title) {
        this.videoTitle = title;
    }

    public int getLastFreezePosition() {
        return lastFreezePosition;
    }

    // -------------------------------------------------------------------
        /*
         * Player Methods below.
         */

    @Override
    public void reset() {
        super.reset();
        state = PlayerState.RESET;
    }

    @Override
    public synchronized void start() throws IllegalStateException {
        if (state == PlayerState.PREPARED
                || state == PlayerState.PAUSED
                || state == PlayerState.STOPPED
                || state == PlayerState.LAGGING
                || state == PlayerState.PLAYBACK_COMPLETE) {
            super.start();
            if (callback != null) {
                // mark playing
                callback.onPlaybackStarted();
            }
            state = PlayerState.PLAYING;
            logger.debug("Playback started");

            // reload controller
            showController();

            requestAccessibilityFocusPausePlay();
        } else {
            logger.warn("Cannot start");
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        if (state == PlayerState.PREPARED
                || state == PlayerState.PLAYING
                || state == PlayerState.PAUSED
                || state == PlayerState.LAGGING
                || state == PlayerState.PLAYBACK_COMPLETE) {
            super.stop();
            state = PlayerState.STOPPED;

            logger.debug("Playback stopped");
        } else {
            logger.warn("Playback cannot stop");
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (isPlaying()) {
            super.pause();
            state = PlayerState.PAUSED;
            if (callback != null) {
                callback.onPlaybackPaused();
            }

            logger.debug("Playback paused");
        } else {
            logger.warn("Playback scannot pause");
        }
    }

    @Override
    public synchronized void seekTo(int msec) throws IllegalStateException {
        int delta = lastCurrentPosition - msec;
        if (delta < 0) {
            delta = delta * (-1);
        }

        if (msec > 0
                && lastCurrentPosition > 0
                && (delta <= 1000)) {
            // no need to perform seek if current position is almost same as seekTo
            // Delta of 1000 is used to skip seek of 1 sec difference from current position
            logger.debug(String.format(Locale.US, "Skipping seek to %d from %d ; state=%s",
                    msec, lastCurrentPosition, state.toString()));
            return;
        }

        if (msec < 0) {
            // cannot seek to invalid position
            msec = 0;
        }
        if (state == PlayerState.PREPARED
                || state == PlayerState.PLAYING
                || state == PlayerState.PAUSED
                || state == PlayerState.STOPPED
                || state == PlayerState.PLAYBACK_COMPLETE
                || state == PlayerState.LAGGING) {
            logger.debug(String.format(Locale.US, "seeking to %d from %d ; state=%s",
                    msec, lastCurrentPosition, state.toString()));
            super.seekTo(msec);
            lastCurrentPosition = msec;
            logger.debug("playback seeked");

            try {
                // wait for a while, so that Player gets into a stable state after seek
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        } else {
            logger.warn("Cannot seek");
        }
    }

    @Override
    public synchronized int getCurrentPosition() {
        try {
            if (state == PlayerState.PREPARED
                    || state == PlayerState.PLAYING
                    || state == PlayerState.PAUSED
                    || state == PlayerState.STOPPED
                    || state == PlayerState.PLAYBACK_COMPLETE
                    || state == PlayerState.LAGGING) {
                lastCurrentPosition = super.getCurrentPosition();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        return lastCurrentPosition;
    }

    @Override
    public int getDuration() {
        try {
            if (state == PlayerState.PREPARED
                    || state == PlayerState.PLAYING
                    || state == PlayerState.PAUSED
                    || state == PlayerState.STOPPED
                    || state == PlayerState.PLAYBACK_COMPLETE
                    || state == PlayerState.LAGGING) {
                lastDuration = super.getDuration();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return lastDuration;
    }

    @Override
    public boolean isPlaying() {
        if (state == PlayerState.PLAYING
                || state == PlayerState.LAGGING) {
            logger.debug("isPlaying = TRUE");
            return super.isPlaying();
        }
        logger.debug("isPlaying = FALSE; state=" + state);
        return false;
    }

    @Override
    public boolean isPaused() {
        if (state == PlayerState.PAUSED) {
            logger.debug("isPaused = TRUE");
            return true;
        }
        logger.debug("isPaused = FALSE; state=" + state);
        return false;
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    @Override
    public void setLMSUrl(String url) {
        this.lmsURL = url;
    }

    @Override
    public void setNextPreviousListeners(OnClickListener next,
                                         OnClickListener prev) {
        if (controller != null) {
            controller.setNextPreviousListeners(next, prev);
        }
    }

    @Override
    public PlayerController getController() {
        return controller;
    }

    @Override
    public void callPlayerSeeked(long previousPos, long nextPos, boolean isRewindClicked) {
        if (callback != null) {
            callback.callPlayerSeeked(previousPos, nextPos, isRewindClicked);
        }
    }
}
