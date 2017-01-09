package org.edx.mobile.player;

import android.graphics.Point;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.io.Serializable;

public interface IPlayer extends Serializable {
    
    long serialVersionUID = 5689385691113719237L;

    void setUri(String uri, int seekTo) throws Exception;
    void setUriAndPlay(String uri, int seekTo) throws Exception;
    void restart() throws Exception;
    void restart(int seekTo) throws Exception;
    boolean isInError();
    boolean isPlayingLocally();
    void start();
    boolean isPlaying();
    boolean isPaused();
    boolean isFrozen();
    void pause();
    int getCurrentPosition();
    void setFullScreen(boolean isFullScreen);
    boolean isFullScreen();
    void setPreview(Preview preview);
    void release();
    void setPlayerListener(IPlayerListener listener);
    void setController(PlayerController controller);
    void freeze();
    void unfreeze();
    void setVideoTitle(String title);
    int getLastFreezePosition();
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
    PlayerController getController();
    boolean isReset();

    // methods from PlayerController.MediaPlayerControl interface
    int     getDuration();
    void    seekTo(int pos);
    int     getBufferPercentage();
    boolean canPause();
    boolean canSeekBackward();
    boolean canSeekForward();
    void    toggleFullScreen();
}
