package org.edx.mobile.module.notification;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hanning on 4/17/15.
 */
public abstract class BaseNotificationPayload {
    private @SerializedName("payload_version") String payloadVersion;

    public String getPayloadVersion() {
        return payloadVersion;
    }

    public void setPayloadVersion(String payloadVersion) {
        this.payloadVersion = payloadVersion;
    }
}
