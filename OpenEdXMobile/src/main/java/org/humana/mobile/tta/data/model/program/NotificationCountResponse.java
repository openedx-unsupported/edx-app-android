package org.humana.mobile.tta.data.model.program;

import android.arch.persistence.room.Entity;

import com.google.gson.annotations.SerializedName;

public class NotificationCountResponse {
    @SerializedName("ReadCount")
    private Long ReadCount;

    @SerializedName("UnReadCount")
    private Long UnReadCount;

    public Long getReadCount() {
        return ReadCount;
    }

    public void setReadCount(Long readCount) {
        ReadCount = readCount;
    }

    public Long getUnReadCount() {
        return UnReadCount;
    }

    public void setUnReadCount(Long unReadCount) {
        UnReadCount = unReadCount;
    }
}
