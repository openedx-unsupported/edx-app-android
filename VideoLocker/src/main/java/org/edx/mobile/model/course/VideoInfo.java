package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VideoInfo implements Serializable {
    @SerializedName("url")
    public String url;

    @SerializedName("file_size")
    public long fileSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoInfo videoInfo = (VideoInfo) o;

        if (fileSize != videoInfo.fileSize) return false;
        return url != null ? url.equals(videoInfo.url) : videoInfo.url == null;

    }
}
