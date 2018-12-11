package org.edx.mobile.player;

import java.io.Serializable;

public interface IPlayerEventCallback extends Serializable {

    void onError();
    void onPlaybackStarted();
    void onPlaybackComplete();
    void saveCurrentPlaybackPosition(long currentPosition);
}
