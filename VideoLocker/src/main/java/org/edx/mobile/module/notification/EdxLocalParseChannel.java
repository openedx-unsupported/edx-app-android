package org.edx.mobile.module.notification;

import java.io.Serializable;

/**
 * need a place to hold the logic of mapping courseId to channelId.
 */
public class EdxLocalParseChannel implements Serializable{

    private String courseId;
    private String channelId;
    private boolean subscribed;
    private boolean operationFailed;

    public EdxLocalParseChannel(){}

    public EdxLocalParseChannel(String courseId, String channelId, boolean subscribed){
        this.courseId = courseId;
        this.channelId = channelId;
        this.subscribed = subscribed;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public boolean isOperationFailed() {
        return operationFailed;
    }

    public void setOperationFailed(boolean operationFailed) {
        this.operationFailed = operationFailed;
    }
}
