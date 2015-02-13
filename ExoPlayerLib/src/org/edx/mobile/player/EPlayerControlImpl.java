package org.edx.mobile.player;

/**
 * @author rohan
 * 
 * This is implementation of PlayerControl class.
 * All the events of player control are forwarded to the player as it is.
 * This class lets player handle every event.
 * 
 */
public class EPlayerControlImpl implements IEPlayerControl {
    
    private IEPlayer player;
    
    public EPlayerControlImpl(IEPlayer player) { 
        this.player = player;
    }

    @Override
    public boolean canPause() {
        return player.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return player.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return player.canSeekForward();
    }

    @Override
    public int getAudioSessionId() {
        return player.getAudioSessionId();
    }

    @Override
    public int getBufferPercentage() {
        return player.getBufferPercentage();
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int positionMs) {
        player.seekTo(positionMs);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return player.isFullScreen();
    }

    @Override
    public void toggleFullScreen() {
        player.toggleFullScreen();
    }

    @Override
    public void setPlaybackSpeed(float speed) {
        player.setPlaybackSpeed(speed);
    }
    
    @Override
    public void setControlPanelAutoHide(boolean autoHide) {
        player.setControlPanelAutoHide(autoHide);
    }

    @Override
    public boolean isControlPanelAutoHide() {
        return player.isControlPanelAutoHide();
    }

    @Override
    public void setVideo(IVideo video) {
        player.setVideo(video);
    }

    @Override
    public IVideo getVideo() {
        return player.getVideo();
    }

    @Override
    public float getPlaybackSpeed() {
        return player.getPlaybackSpeed();
    }
}
