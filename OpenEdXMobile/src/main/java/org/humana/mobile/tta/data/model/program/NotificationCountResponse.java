package org.humana.mobile.tta.data.model.program;

import android.arch.persistence.room.Entity;

public class NotificationCountResponse {
    private Long ReadCount;
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
