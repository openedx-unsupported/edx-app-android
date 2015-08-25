package org.edx.mobile.event;

/**
 * Created by hanning on 4/16/15.
 */
public class ServerSideDataChangedEvent extends BaseEvent{
    public static enum EventType { RESPONSE_ADDED, COMMENT_ADDED }

    public final EventType type;
    public final Object value;

    public ServerSideDataChangedEvent(EventType type){
       this( type, null);
    }

    public ServerSideDataChangedEvent(EventType type, Object value){
        this.type = type;
        this.value = value;
    }
}
