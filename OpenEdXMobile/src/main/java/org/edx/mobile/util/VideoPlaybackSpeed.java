package org.edx.mobile.util;


/**
 * This enum defines the video playback speed constants for {@link org.edx.mobile.player.VideoPlayer VideoPlayer}
 */
public enum VideoPlaybackSpeed {
    SLOW(0.5f), NORMAL(1.0f),
    FAST(1.5f), VERY_FAST(2.0f);

    private final float speedValue;

    // Constructor
    VideoPlaybackSpeed(final float speedValue) {
        this.speedValue = speedValue;
    }

    public float getSpeedValue() {
        return this.speedValue;
    }
}
