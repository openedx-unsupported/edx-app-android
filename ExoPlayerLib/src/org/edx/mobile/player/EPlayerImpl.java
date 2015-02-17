package org.edx.mobile.player;

import com.google.android.exoplayer.ExoPlayer;

public class EPlayerImpl implements IEPlayer {

    private ExoPlayer       exoPlayer;
    private IVideo          mVideo;
    private boolean         isFullScreen = false;
    private boolean         isControlPanelAutoHide = true;

    public EPlayerImpl(ExoPlayer player) {
        this.exoPlayer = player;
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    @Override
    public void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        
        updateOrientation(isFullScreen);
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        // 0 (zero) represents error
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return exoPlayer.getBufferedPercentage();
    }

    @Override
    public int getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return exoPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(int positionMs) {
        // handle boundary conditions for the seek operation
        if (positionMs < 0) {
            // minimum seek position
            positionMs = 0;
        }
        if (positionMs > getDuration()) {
            // maximum seek position
            positionMs = getDuration();
        }
        
        exoPlayer.seekTo(positionMs);
    }

    @Override
    public void start() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void setPlaybackSpeed(float speed) {
        exoPlayer.setPlaybackSpeed(speed);
    }

    private void updateOrientation(boolean isFullScreen) {
        // TODO Player display should be shown in 
        // landscape or portrait as per the value of isFullScreen flag
    }

    @Override
    public void setControlPanelAutoHide(boolean autoHide) {
        this.isControlPanelAutoHide = autoHide;
    }

    @Override
    public boolean isControlPanelAutoHide() {
        return isControlPanelAutoHide;
    }

    @Override
    public void setVideo(IVideo video) {
        this.mVideo = video;
    }

    @Override
    public IVideo getVideo() {
        return mVideo;
    }
    
    @Override
    public float getPlaybackSpeed() {
        return exoPlayer.getPlaybackSpeed();
    }
    
    @Override
    public void release() {
        exoPlayer.release();
    }
    
    @Override
    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }
}
