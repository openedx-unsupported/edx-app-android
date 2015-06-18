package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.model.api.TranscriptModel;

/**
 *
 */
public class VideoData extends BlockData {

    @SerializedName("duration")
    public long duration;

    @SerializedName("transcripts")
    public TranscriptModel transcripts;

    @SerializedName("only_on_web")
    public boolean onlyOnWeb;

    @SerializedName("encoded_videos")
    public EncodedVideos encodedVideos;

}
