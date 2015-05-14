package org.edx.mobile.event;

/**
 * Created by hanning on 4/16/15.
 */
public class DownloadEvent extends BaseEvent{
    public static enum DownloadStatus { NOT_STARTED, STARTED, PAUSED, COMPLETED}

    private DownloadStatus status;

    public DownloadEvent(DownloadStatus status){
        this.status = status;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }
}
