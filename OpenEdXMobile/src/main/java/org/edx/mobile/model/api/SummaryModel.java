package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.util.JavaUtil;

import java.io.Serializable;


@SuppressWarnings("serial")
public class SummaryModel implements Serializable {

    private BlockType category;
    private String name;

    @SerializedName("video_url")
    private String videoUrl;
    @SerializedName("video_thumbnail_url")
    private String videoThumbnailUrl;
    private double duration;
    @SerializedName("only_on_web")
    public boolean onlyOnWeb;
    private String id;
    private long size;
    @SerializedName("transcripts")
    private TranscriptModel transcripts;
    private EncodingsModel encodings;

    public BlockType getType() {
        return category;
    }

    public void setType(BlockType category) {
        this.category = category;
    }

    public String getDisplayName() {
        return name;
    }

    public void setDisplayName(String name) {
        this.name = name;
    }

    public String getVideoUrl() {
        if (videoUrl == null)
            return null;
        return videoUrl.trim();
    }

    public void setVideoUrl(String video_url) {
        this.videoUrl = video_url;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String video_thumbnail_url) {
        this.videoThumbnailUrl = video_thumbnail_url;
    }

    public boolean isOnlyOnWeb() {
        return onlyOnWeb;
    }

    public void setOnlyOnWeb(boolean only_on_web) {
        this.onlyOnWeb = only_on_web;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public long getSize() {
        return size;
    }

    public String getSizeString() { 
        return JavaUtil.getMemorySize(size);
    }
    
    public void setSize(long size) {
        this.size = size;
    }

    public TranscriptModel getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(TranscriptModel transcripts) {
        this.transcripts = transcripts;
    }

    public String getHighEncoding() {
        return this.encodings == null ? null : encodings.highEncoding;
    }

    public String getLowEncoding() {
        return this.encodings == null ? null : encodings.lowEncoding;
    }

    public String getYoutubeLink() {
        return this.encodings == null ? null : encodings.youtubeLink;
    }


    
    public int getDuration() {
        return (int)duration;
    }
}
