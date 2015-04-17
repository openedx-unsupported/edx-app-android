package org.edx.mobile.event;

/**
 * Created by hanning on 4/17/15.
 */
public class CourseAnnouncementEvent extends BaseEvent{
    public static enum EventType { MESSAGE_RECEIVED, MESSAGE_TAPPED }

    public final EventType type;
    public final String courseId;

    public CourseAnnouncementEvent(EventType type, String courseId){
        this.type = type;
        this.courseId = courseId;
    }
}
