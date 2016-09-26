package org.edx.mobile.player;

import android.view.View;

import java.io.Serializable;

public interface IPlayerEventCallback extends Serializable {

    void onError();
    void onPlaybackStarted();
    void onPlaybackComplete();
    void saveCurrentPlaybackPosition(int currentPosition);
}
