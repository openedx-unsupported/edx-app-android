package org.edx.mobile.player;

import android.graphics.Point;

public interface IPlayerListener {

    public void onError();
    public void onVideoLagging();
    public void onVideoNotSeekable();
    public void onPreparing();
    public void onPrepared();
    public void onPlaybackPaused();
    public void onPlaybackStarted();
    public void onPlaybackComplete();
    public void onFullScreen(boolean isFullScreen);
    public void callLMSServer(String lmsUrl);
    public void callSettings(Point p);
    public void callPlayerSeeked(long lastPostion, long newPosition, boolean isRewindClicked);
}
