package org.edx.mobile.event;

import androidx.annotation.NonNull;

/**
 * An event fired on application start in case the app updated to a newer version.
 */
public class AppUpdatedEvent {
    @NonNull
    private final long previousVersionCode;
    @NonNull
    private final long curVersionCode;
    @NonNull
    private final String previousVersionName;
    @NonNull
    private final String curVersionName;

    public AppUpdatedEvent(@NonNull long previousVersionCode, @NonNull long currVersionCode,
                           @NonNull String previousVersionName, @NonNull String curVersionName) {
        this.previousVersionCode = previousVersionCode;
        this.curVersionCode = currVersionCode;
        this.previousVersionName = previousVersionName;
        this.curVersionName = curVersionName;
    }

    @NonNull
    public long getPreviousVersionCode() {
        return previousVersionCode;
    }

    @NonNull
    public long getCurVersionCode() {
        return curVersionCode;
    }

    @NonNull
    public String getPreviousVersionName() {
        return previousVersionName;
    }

    @NonNull
    public String getCurVersionName() {
        return curVersionName;
    }
}
