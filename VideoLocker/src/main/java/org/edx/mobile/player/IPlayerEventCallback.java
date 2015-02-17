package org.edx.mobile.player;

import java.io.Serializable;

public interface IPlayerEventCallback extends Serializable {

    public void onError();
    public void onPlaybackStarted();
    public void onPlaybackComplete();
    public void saveCurrentPlaybackPosition(int currentPosition);
}
