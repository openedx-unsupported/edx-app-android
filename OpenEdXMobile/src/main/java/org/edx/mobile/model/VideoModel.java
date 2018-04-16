package org.edx.mobile.model;

import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.download.NativeDownloadModel;

/**
 * Any video model should implement this interface.
 * Database model should also implement this interface.
 * @author rohan
 *
 *
 */

public interface VideoModel {

    String getUsername();

    String getTitle();

    String getVideoId();

    long getSize();

    long getDuration();

    String getFilePath();

    String getVideoUrl();

    String getHLSVideoUrl();

    String getHighQualityVideoUrl();

    String getLowQualityVideoUrl();

    String getYoutubeVideoUrl();

    int getWatchedStateOrdinal();

    int getDownloadedStateOrdinal();

    long getDmId();

    String getEnrollmentId();

    String getChapterName();

    String getSectionName();

    int getLastPlayedOffset();

    String getLmsUrl();
    
    boolean isCourseActive();

    boolean isVideoForWebOnly();

    long getDownloadedOn();
    
    TranscriptModel getTranscripts();
    //TODO: write all required method of the video model

    /**
     * Sets download information from the given download object.
     * @param download
     */
    void setDownloadInfo(NativeDownloadModel download);

    /**
     * Sets download information from the given video object.
     * @param videoByUrl
     */
    void setDownloadInfo(VideoModel videoByUrl);
    
    /**
     * Sets downloading information from the given download object.
     * @param download
     */
    void setDownloadingInfo(NativeDownloadModel download);
    
}
