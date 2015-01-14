package org.edx.mobile.model.api;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LatestUpdateModel implements Serializable {
    private String video;

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

}
