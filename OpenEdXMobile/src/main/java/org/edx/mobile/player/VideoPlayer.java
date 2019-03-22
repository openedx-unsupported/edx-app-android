package org.edx.mobile.player;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.view.View.OnClickListener;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.VideoUtil;
import org.edx.mobile.view.OnSwipeListener;

import java.util.Locale;

import static org.edx.mobile.util.AppConstants.VIDEO_FORMAT_M3U8;

@SuppressWarnings("serial")
public class VideoPlayer implements Player.EventListener, AnalyticsListener, PlayerListener {

    private SimpleExoPlayer exoPlayer;
    private Context context;

    // Player states
    public enum PlayerState {
        RESET, URI_SET, PREPARED,
        PLAYING, PAUSED, ERROR, LAGGING, PLAYBACK_COMPLETE, STOPPED
    }

    private PlayerState state;
    private boolean isFullScreen;
    private boolean isPlayingLocally;
    private boolean playWhenPrepared;
    private boolean autoHideControls;
    private transient IPlayerListener callback;
    private transient PlayerController controller;
    private long lastCurrentPosition;
    private long lastFreezePosition;
    private long lastDuration;
    private boolean isFrozen;
    private PlayerState freezeState;
    private long seekToWhenPrepared;
    private String videoTitle;
    private String lmsURL;
    private String videoUri;
    private static final Logger logger = new Logger(VideoPlayer.class.getName());

    public VideoPlayer(Context context) {
        init(context);
        initExoPlayer();
    }

    /**
     * Resets all the fields of this player.
     *
     * @param context
     */
    private void init(Context context) {
        this.context = context;
        this.state = PlayerState.RESET;
        this.isFullScreen = false;
        this.isPlayingLocally = true;
        this.playWhenPrepared = false;
        this.lastCurrentPosition = 0;
        this.lastDuration = 0;
        this.isFrozen = false;
        this.autoHideControls = true;
    }

    private void initExoPlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this.context);
        exoPlayer.addListener(this);
        exoPlayer.addAnalyticsListener(this);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
    }

    @Override
    public int getBufferPercentage() {
        return exoPlayer.getBufferedPercentage();
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
                && isSeekable()) {
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
                && isSeekable()) {
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
    public boolean isSeekable() {
        final boolean isSeekable = exoPlayer.getCurrentTimeline()
                .getWindow(exoPlayer.getCurrentWindowIndex(), new Timeline.Window())
                .isSeekable;
        if (!isSeekable) {
            logger.debug("Track not seekable");
            if (callback != null)
                callback.onVideoNotSeekable();
        }
        return isSeekable;
    }

    @Override
    public void callSettings(Point p) {
        if (callback != null) {
            callback.callSettings(p);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_READY:
                state = PlayerState.PREPARED;
                if (callback != null) {
                    callback.onPrepared();
                }

                if (playWhenReady) {
                    state = PlayerState.PLAYING;
                }
                break;
            case Player.STATE_ENDED:
                // onPlayerStateChanged with Player.STATE_ENDED called twice after calling
                // setPlayWhenReady(false) in STATE_ENDED
                // so if already called then avoid on completion again.
                if (state == PlayerState.PLAYBACK_COMPLETE)
                    return;
                // sometimes, error also causes onCompletion() call
                // avoid on completion if player got an error
                if (state != PlayerState.ERROR) {
                    state = PlayerState.PLAYBACK_COMPLETE;
                    if (callback != null) {
                        callback.onPlaybackComplete();
                    }
                    seekTo(0);
                    exoPlayer.setPlayWhenReady(false);
                    logger.debug("Playback complete");
                }
                break;
        }
    }

    @Override
    public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {
        state = PlayerState.LAGGING;
        if (callback != null) {
            callback.onVideoLagging();
        }
        logger.debug("Video track lagging");
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (lastCurrentPosition != 0) {
            seekToWhenPrepared = lastCurrentPosition;
        }
        state = PlayerState.ERROR;
        if (callback != null) {
            callback.onError();
        }

        if (error.type == ExoPlaybackException.TYPE_UNEXPECTED) {
            logger.warn("ERROR: unexpected");
        } else if (error.type == ExoPlaybackException.TYPE_RENDERER) {
            logger.warn("ERROR: renderer");
        } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            logger.warn("ERROR: occurred while loading data from MediaSource");
        }
        logger.warn("ERROR: type=" + error.type + ";message=" + error.getMessage());
    }

    @Override
    public void setUri(String uri, long seekTo) {
        load(uri, seekTo, false);
    }

    @Override
    public void setUriAndPlay(String uri, long seekTo) {
        load(uri, seekTo, true);
    }

    @Override
    public void restart(long seekTo) {
        logger.debug("RestartFreezePosition=" + seekTo);
        lastCurrentPosition = 0;
        // if seekTo=lastCurrentPosition then seekTo() method will not work
        load(videoUri, seekTo, playWhenPrepared);
    }

    @Override
    public void restart() {
        restart(seekToWhenPrepared);
    }

    private void load(String videoUri, long seekTo, boolean playWhenPrepared) {
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

        if (videoUri != null) {
            final MediaSource mediaSource = getMediaSource(videoUri);
            exoPlayer.prepare(mediaSource);
            state = PlayerState.URI_SET;
            isPlayingLocally = !videoUri.startsWith("http");
            // notify that the player is now preparing
            if (callback != null) {
                callback.onPreparing();
            }
        }
    }

    /**
     * Function that provides the media source played by ExoPlayer based on media type.
     *
     * @param videoUrl Video URL
     * @return The {@link MediaSource} to play.
     */
    private MediaSource getMediaSource(String videoUrl) {
        final String userAgent = Util.getUserAgent(this.context, this.context.getString(R.string.app_name));
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this.context, userAgent);
        final MediaSource mediaSource;

        if (VideoUtil.videoHasFormat(videoUrl, VIDEO_FORMAT_M3U8)) {
            mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(videoUrl));
        } else {
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(videoUrl));
        }
        return mediaSource;
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
    public void setPlayerView(final PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        playerView.setPlayer(exoPlayer);
        // hide default controls of Exo Player
        playerView.setUseController(false);
        logger.debug("Player state=" + state);
        if (!isPlaying()) {
            seekTo(lastCurrentPosition);
        }
        playerView.setOnTouchListener(new OnSwipeListener(playerView.getContext()) {

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
    public void release() {
        exoPlayer.removeListener(this);
        exoPlayer.removeAnalyticsListener(this);
        exoPlayer.release();
    }

    @Override
    public void setPlayerListener(IPlayerListener listener) {
        this.callback = listener;
    }

    @Override
    public void setController(PlayerController cont) {
        if (this.controller != null) {
            if (cont == null)
                this.controller.setMediaPlayer(null);
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

    /**
     * Controls will be hidden immediately if changed to false
     */
    @Override
    public void setAutoHideControls(boolean autoHide) {
        if (!autoHide && autoHideControls) {
            hideController();
        }
        autoHideControls = autoHide;
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
            } else {
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

        setPlayerView(null);
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

    public long getLastFreezePosition() {
        return lastFreezePosition;
    }

    // -------------------------------------------------------------------
    /*
     * Player Methods below.
     */

    @Override
    public void reset() {
        // stop and reset Exo Player
        exoPlayer.stop(true);
        state = PlayerState.RESET;
    }

    @Override
    public synchronized void start() throws IllegalStateException {
        if (state == PlayerState.PREPARED
                || state == PlayerState.PAUSED
                || state == PlayerState.STOPPED
                || state == PlayerState.LAGGING
                || state == PlayerState.PLAYBACK_COMPLETE) {
            if (seekToWhenPrepared > 0) {
                seekTo(seekToWhenPrepared);
            }
            exoPlayer.setPlayWhenReady(true);
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
            // Stop the Exo Player
            exoPlayer.stop(false);
            state = PlayerState.STOPPED;

            logger.debug("Playback stopped");
        } else {
            logger.warn("Playback cannot stop");
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (isPlaying()) {
            seekToWhenPrepared = getCurrentPosition();
            // Pause media playback
            exoPlayer.setPlayWhenReady(false);
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
    public synchronized void seekTo(long msec) throws IllegalStateException {
        long delta = lastCurrentPosition - msec;
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
            exoPlayer.seekTo(msec);
            lastCurrentPosition = msec;
            seekToWhenPrepared = msec;

            logger.debug("playback seeked");
        } else {
            logger.warn("Cannot seek");
        }
    }

    @Override
    public synchronized long getCurrentPosition() {
        try {
            if (state == PlayerState.PREPARED
                    || state == PlayerState.PLAYING
                    || state == PlayerState.PAUSED
                    || state == PlayerState.STOPPED
                    || state == PlayerState.PLAYBACK_COMPLETE
                    || state == PlayerState.LAGGING) {
                lastCurrentPosition = exoPlayer.getCurrentPosition();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        return lastCurrentPosition;
    }

    @Override
    public long getDuration() {
        try {
            if (state == PlayerState.PREPARED
                    || state == PlayerState.PLAYING
                    || state == PlayerState.PAUSED
                    || state == PlayerState.STOPPED
                    || state == PlayerState.PLAYBACK_COMPLETE
                    || state == PlayerState.LAGGING) {
                lastDuration = exoPlayer.getDuration();
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
            return (exoPlayer.getPlaybackState() == Player.STATE_READY ||
                    exoPlayer.getPlaybackState() == Player.STATE_BUFFERING)
                    && exoPlayer.getPlayWhenReady();
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

    @Override
    public void setPlaybackSpeed(float speed) {
        if (exoPlayer != null) {
            exoPlayer.setPlaybackParameters(new PlaybackParameters(speed));
        }
    }
}
