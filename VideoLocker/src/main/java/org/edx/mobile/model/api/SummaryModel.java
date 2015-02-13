package org.edx.mobile.model.api;

import java.io.Serializable;


@SuppressWarnings("serial")
public class SummaryModel implements Serializable {

    private String category;
    private String name;
    private String video_url;
    private String video_thumbnail_url;
    private double duration;
    private String id;
    private long size;
    private TranscriptModel transcripts;
    private EncodingsModel encodings;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideo_url() {
        if (video_url == null)
            return null;
        return video_url.trim();
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_thumbnail_url() {
        return video_thumbnail_url;
    }

    public void setVideo_thumbnail_url(String video_thumbnail_url) {
        this.video_thumbnail_url = video_thumbnail_url;
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
        return getMemorySize(size);
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

    private String getMemorySize(long bytes) {
        if (bytes == 0) {
            return "0KB";
        }
        
        long s = bytes;
        int gb = (int) (s / (1024f * 1024f * 1024f) );
        s = s % (1024 * 1024 * 1024) ;
        int mb = (int) (s / (1024f * 1024f) );
        s = s % (1024 * 1024) ;
        int kb = (int) (s / 1024f);
        int b = (int) (s % 1024);
        
        return String.format("%d MB", mb);
    }
    
    public int getDuration() {
        return (int)duration;
    }
    
    /**
     * Returns duration in the format hh:mm:ss
     * @return
     */
    public String getDurationString() {
        if (duration == 0) {
            return "00:00";
        }
        
        long d = (long)duration;
        int hours = (int) (d / 3600f); 
        d = d % 3600;
        int mins = (int) (d / 60f);
        int secs = (int) (d % 60); 
        if (hours <= 0) {
            return String.format("%02d:%02d", mins, secs);
        }
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }
}
