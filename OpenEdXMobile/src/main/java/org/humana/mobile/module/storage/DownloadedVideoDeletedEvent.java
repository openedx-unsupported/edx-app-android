package org.humana.mobile.module.storage;

import org.humana.mobile.model.VideoModel;

public class DownloadedVideoDeletedEvent {

    private VideoModel model;

    public DownloadedVideoDeletedEvent(VideoModel model) {
        this.model = model;
    }

    public VideoModel getModel() {
        return model;
    }
}
