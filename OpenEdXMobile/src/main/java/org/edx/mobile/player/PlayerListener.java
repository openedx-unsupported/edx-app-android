package org.edx.mobile.player;

import android.graphics.Point;
import android.view.View;

import com.google.android.exoplayer2.ui.PlayerView;

import java.io.Serializable;

public interface PlayerListener extends Serializable {

    long serialVersionUID = 5689385691113719237L;

    void setUri(String uri, long seekTo) throws Exception;
    void setUriAndPlay(String uri, long seekTo) throws Exception;
    void restart() throws Exception;
    void restart(long seekTo) throws Exception;
    boolean isInError();
    boolean isPlayingLocally();
    void start();
    boolean isPlaying();
    boolean isPaused();
    boolean isFrozen();
    void stop();
    void pause();
    long getCurrentPosition();
    void setFullScreen(boolean isFullScreen);
    boolean isFullScreen();
    void setPlayerView(PlayerView preview);
    void release();
    void setPlayerListener(IPlayerListener listener);
    void setController(PlayerController controller);
    void freeze();
    void unfreeze();
    void setVideoTitle(String title);
    long getLastFreezePosition();
    void setAutoHideControls(boolean autoHide);
    boolean getAutoHideControls();
    void showController();
    void hideController();
    void requestAccessibilityFocusPausePlay();
    void reset();
    void setLMSUrl(String url);
    void setNextPreviousListeners(View.OnClickListener next, View.OnClickListener prev);
    void callSettings(Point p);
    void callPlayerSeeked(long previousPos, long nextPos, boolean isRewindClicked);
    void setPlaybackSpeed(float speed);
    PlayerController getController();
    boolean isReset();

    // methods from PlayerController.MediaPlayerControl interface
    long    getDuration();
    void    seekTo(long pos);
    int     getBufferPercentage();
    boolean canPause();
    boolean canSeekBackward();
    boolean canSeekForward();
    void    toggleFullScreen();
    boolean  isSeekable();
}
