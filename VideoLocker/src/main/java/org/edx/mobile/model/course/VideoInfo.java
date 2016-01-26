package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VideoInfo implements Serializable{
    @SerializedName("url")
    public String url;

    @SerializedName("file_size")
    public long fileSize;
}
