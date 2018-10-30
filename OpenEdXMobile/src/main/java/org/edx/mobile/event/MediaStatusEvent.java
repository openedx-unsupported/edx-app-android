package org.edx.mobile.event;

public class MediaStatusEvent extends BaseEvent {

    private final boolean sdCardAvailable;

    public MediaStatusEvent(boolean sdCardAvailable) {
        this.sdCardAvailable = sdCardAvailable;
    }

    public boolean isSdCardAvailable() {
        return sdCardAvailable;
    }
}
