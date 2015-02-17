package org.edx.mobile.player;

import android.widget.MediaController.MediaPlayerControl;

/**
 * @author rohan
 * 
 * This class represents custom {@link MediaPlayerControl}.
 * This class has all the build-in controls, additionally a few controls that edX app requires.
 *
 */
public interface IEPlayerControl extends MediaPlayerControl {

    /**
     * Returns true if player is in full screen (Landscape) mode, false otherwise.
     * @return
     */
    boolean isFullScreen();
    
    /**
     * Toggles player orientation between Landscape and Portrait views.
     */
    void    toggleFullScreen();
    
    /**
     * Sets playback speed for the player.
     * This method can be called while player is in any state.
     * @param speed
     */
    void    setPlaybackSpeed(float speed);
    
    /**
     * If autoHide is true, then control panel of the player fades out 
     * automatically after 3 seconds.
     * If autoHide is false, then control panel doesn't fade out automatically.
     * Tapping on the player preview toggles visibility of the control panel.
     * @param autoHide
     */
    void    setControlPanelAutoHide(boolean autoHide);

    /**
     * Returns true if autoHide is enabled, false otherwise.
     * By default, autoHide is enabled for control panel. Control panel fades out after 3 seconds.
     * @return
     */
    boolean isControlPanelAutoHide();
    
    /**
     * Sets video model to the player.
     * @param video
     */
    void    setVideo(IVideo video);
    
    /**
     * Returns currently playing video model.
     * @return
     */
    IVideo  getVideo();

    /**
     * Returns current playback speed.
     * @return
     */
    float getPlaybackSpeed();
}
