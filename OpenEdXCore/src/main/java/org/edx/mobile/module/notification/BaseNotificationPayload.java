package org.edx.mobile.module.notification;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public abstract class BaseNotificationPayload {
    private @SerializedName("action") String action;
    private @SerializedName("push_hash") String pushHash;
    private @SerializedName("notification-id") String notificationId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPushHash() {
        return pushHash;
    }

    public void setPushHash(String pushHash) {
        this.pushHash = pushHash;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getIdentifier(){
        return TextUtils.isEmpty(notificationId) ? pushHash : notificationId;
    }

}
