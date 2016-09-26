package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by yervant on 1/15/15.
 */
public class EncodingsModel implements Serializable{

    @SerializedName("high")
    public String highEncoding;

    @SerializedName("low")
    public String lowEncoding;

    @SerializedName("youtube")
    public String youtubeLink;

    public enum EncodingLevel{
        HIGH,
        LOW,
    }
}
