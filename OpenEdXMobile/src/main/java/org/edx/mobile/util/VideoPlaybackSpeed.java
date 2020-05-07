package org.edx.mobile.util;


/**
 * This enum defines the video playback speed constants for {@link org.edx.mobile.player.VideoPlayer VideoPlayer}
 */
public enum VideoPlaybackSpeed {
    SPEED_0_25X(0.25f), SPEED_0_50X(0.5f), SPEED_0_75X(0.75f),
    SPEED_1_0X(1.0f), SPEED_1_25X(1.25f), SPEED_1_50X(1.5f),
    SPEED_1_75X(1.75f), SPEED_2_0X(2.0f);

    private final float speedValue;

    // Constructor
    VideoPlaybackSpeed(final float speedValue) {
        this.speedValue = speedValue;
    }

    public float getSpeedValue() {
        return this.speedValue;
    }
}
