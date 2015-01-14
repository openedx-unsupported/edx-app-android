package com.github.axet.vget.info;

import com.github.axet.vget.info.VideoInfo.VideoQuality;

public class VideoInfoUser {
    private VideoQuality userQuality;

    public VideoQuality getUserQuality() {
        return userQuality;
    }

    /**
     * limit maximum quality, or do not call this function if you wish maximum
     * quality available. if youtube does not have video with requested quality,
     * program will raise an exception
     * 
     * @param userQuality
     */
    public void setUserQuality(VideoQuality userQuality) {
        this.userQuality = userQuality;
    }

}