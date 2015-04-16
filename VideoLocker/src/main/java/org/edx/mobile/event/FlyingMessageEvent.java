package org.edx.mobile.event;

/**
 * Created by hanning on 4/16/15.
 */
public class FlyingMessageEvent extends BaseEvent{
    public static enum MessageType { ERROR, INFO }

    public final MessageType type;
    public final String title;
    public final String message;

    public FlyingMessageEvent(String message){
        this(MessageType.INFO, null, message);
    }

    public FlyingMessageEvent(String title, String message){
        this(MessageType.INFO, title, message);
    }

    public FlyingMessageEvent(MessageType type, String title, String message){
        this.type = type;
        this.title = title;
        this.message = message;
    }


}
