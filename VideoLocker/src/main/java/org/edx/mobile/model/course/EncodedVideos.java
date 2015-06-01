package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by hanning on 5/19/15.
 */
public class EncodedVideos implements Serializable{
    @SerializedName("fallback")
    public VideoInfo fallback;

    @SerializedName("mobile_high")
    public VideoInfo mobileHigh;

    @SerializedName("mobile_low")
    public VideoInfo mobileLow;

    @SerializedName("youtube")
    public VideoInfo youtube;

    public VideoInfo getPreferredVideoInfo(){
        if ( mobileLow != null )
            return mobileLow;
        if ( mobileHigh != null )
            return mobileHigh;
        if ( fallback != null)
            return fallback;
        return new VideoInfo(); //should not be here
    }

}
