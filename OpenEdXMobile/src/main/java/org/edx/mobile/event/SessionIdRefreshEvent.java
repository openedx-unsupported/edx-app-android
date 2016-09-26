package org.edx.mobile.event;

/**
 * Created by hanning on 4/16/15.
 */
public class SessionIdRefreshEvent extends BaseEvent {
    public final boolean success;

    public SessionIdRefreshEvent(boolean success) {
        this.success = success;
    }

}
