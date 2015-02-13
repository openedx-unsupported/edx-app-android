package org.edx.mobile.player;

import com.google.android.exoplayer.ExoPlayer;


/**
 * Player must implement all the methods of PlayerControl class.
 * @author rohan
 *
 */
public interface IEPlayer extends IEPlayerControl {

    void release();
    
    ExoPlayer getExoPlayer();

    /**
     * Sets preview display for the player.
     * @param surfaceView
     */
//  void setDisplay(VideoSurfaceView surfaceView);

}
