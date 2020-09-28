package org.humana.mobile.model.db;

import android.content.Context;
import android.text.TextUtils;

import com.google.inject.Inject;

import org.humana.mobile.R;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.interfaces.SectionItemInterface;
import org.humana.mobile.model.VideoModel;
import org.humana.mobile.model.api.EncodingsModel;
import org.humana.mobile.model.api.TranscriptModel;
import org.humana.mobile.model.download.NativeDownloadModel;
import org.humana.mobile.module.prefs.PrefManager;
import org.humana.mobile.tta.data.enums.DownloadType;
import org.humana.mobile.tta.wordpress_client.model.CustomFilter;
import org.humana.mobile.tta.wordpress_client.model.Post;
import org.humana.mobile.tta.wordpress_client.util.MxFilterType;
import org.humana.mobile.util.BrowserUtil;
import org.humana.mobile.util.JavaUtil;

public class DownloadEntry implements SectionItemInterface, VideoModel {

    public static enum WatchedState { UNWATCHED, PARTIALLY_WATCHED, WATCHED}
    public static enum DownloadedState { DOWNLOADING, DOWNLOADED, ONLINE }

    public int id;
    public String username;
    public String title;
    public String filepath;
    public long size;
    public long duration;
    // default unwatched
    public WatchedState watched = WatchedState.UNWATCHED;
    // default not_downloaded
    public DownloadedState downloaded = DownloadedState.ONLINE;
    public String videoId;
    public String url;
    public String url_hls;
    public String url_high_quality;
    public String url_low_quality;
    public String url_youtube;
    public long dmId = -1;

    public String type;
    public long content_id;

    // enrollment id
    public String eid;
    public String chapter;
    public String section;
    public long downloadedOn;
    public int lastPlayedOffset;
    public int isCourseActive = 1; // default is TRUE
    public boolean isVideoForWebOnly; //default is FALSE
    public String lmsUrl;
    public TranscriptModel transcript;
    public String scormUploadedOn;
    @Inject
    IEdxEnvironment environment;

    /**
     * Returns duration in the readable format i.e. hh:mm:ss. Returns null if duration is zero or
     * negative.
     *
     * @return Formatted duration.
     */
    public String getDurationReadable() {
        return JavaUtil.getDurationString(duration);
    }

    @Override
    public boolean isChapter() {
        // video model is never a chapter
        return false;
    }

    @Override
    public boolean isSection() {
        // video model is never a section
        return false;
    }
    
    @Override
    public boolean isCourse() {
        return false;
    }

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public boolean isDownload() {
        return true;
    }
    
    /**
     * Returns true if this video is downloaded successfully, false otherwise.
     * @return
     */
    public boolean isDownloaded() {
        return (downloaded == DownloadedState.DOWNLOADED);
    }
    
    @Override
    public String toString() {
        return String.format("dmid=%d, title=%s, path=%s, url=%s, size=%d, duration=%d", dmId, title, filepath, url, size, duration);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getTitle() {
        if (title == null || title.trim().length() == 0) {
            return "(Untitled)";
        }
        return title;
    }
    @Override
    public String getScormUploadedOn() {
        return scormUploadedOn;
    }
    @Override
    public String getVideoId() {
        return videoId;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getFilePath() {
        return filepath;
    }

    @Override
    public boolean getattachType() {
        return type != null && (type.equalsIgnoreCase(DownloadType.SCORM.name()) ||
                type.equalsIgnoreCase(DownloadType.PDF.name()) ||
                type.equalsIgnoreCase(DownloadType.WP_VIDEO.name()));
    }

    public long getContent_id() {
        return content_id;
    }

    public void setContent_id(long content_id) {
        this.content_id = content_id;
    }

    @Override
    public String getDownloadType() {
        return type;
    }

    @Override
    public String getVideoUrl() {
        return url;
    }

    @Override
    public String getHLSVideoUrl() {
        return url_hls;
    }

    @Override
    public String getHighQualityVideoUrl() {
        return url_high_quality;
    }

    @Override
    public String getLowQualityVideoUrl() {
        return url_low_quality;
    }

    @Override
    public String getYoutubeVideoUrl() {
        return url_youtube;
    }

    @Override
    public int getWatchedStateOrdinal() {
        return watched.ordinal();
    }

    @Override
    public int getDownloadedStateOrdinal() {
        return downloaded.ordinal();
    }

    @Override
    public long getDmId() {
        return dmId;
    }

    @Override
    public String getEnrollmentId() {
        return eid;
    }

    @Override
    public String getChapterName() {
        return chapter;
    }

    @Override
    public String getSectionName() {
        return section;
    }

    @Override
    public int getLastPlayedOffset() {
        return lastPlayedOffset;
    }

    @Override
    public String getLmsUrl() {
        return lmsUrl;
    }

    @Override
    public boolean isCourseActive() {
        return (isCourseActive == 1);
    }

    @Override
    public boolean  isVideoForWebOnly() { return isVideoForWebOnly; }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public long getDownloadedOn() {
        return downloadedOn;
    }

    @Override
    public TranscriptModel getTranscripts() {
        return transcript;
    }

    @Override
    public void setDownloadInfo(NativeDownloadModel download) {
        dmId = download.dmid;
        downloaded = DownloadedState.DOWNLOADING;
        filepath = download.filepath;
        size = download.size;
        // duration can't be updated here
    }
    
    @Override
    public void setDownloadingInfo(NativeDownloadModel download) {
        dmId = download.dmid;
        downloaded = DownloadedState.DOWNLOADING;
        // duration can't be updated here
    }

    //added by Arjun for Scrom handeling
    @Override
    public void setDownloadedStateForScrom(DownloadedState downloadedState) {
        downloaded = downloadedState;
    }

    @Override
    public void setDownloadInfo(VideoModel video) {
        dmId = video.getDmId();
        downloaded = DownloadedState.values()[video.getDownloadedStateOrdinal()];
        filepath = video.getFilePath();
        size = video.getSize();
        duration = video.getDuration();
    }

    public String getBestEncodingUrl(Context context) {
        if (!TextUtils.isEmpty(url_hls)) {
            return url_hls;
        }

        PrefManager prefs = new PrefManager(context, PrefManager.Pref.WIFI);
        float kbs = prefs.getFloat(PrefManager.Key.SPEED_TEST_KBPS, 0.0f);
        float thresholdKps = (float)context.getResources().getInteger(R.integer.threshold_kbps_for_video);

        EncodingsModel.EncodingLevel level = kbs > thresholdKps ?
                EncodingsModel.EncodingLevel.HIGH : EncodingsModel.EncodingLevel.LOW;

        switch (level) {
            case HIGH:
                if (!TextUtils.isEmpty(url_high_quality)){
                    return url_high_quality;
                }
                break;
            case LOW:
                if (!TextUtils.isEmpty(url_low_quality)){
                    return url_low_quality;
                }
                break;
        }

        return getVideoUrl();
    }

    public void setDownloadEntryForScrom(String username,String  title,String  filepath,String  videoId,
                                         String  url,String  eid,String  chapter,
                                         String  section,Long  downloadedOn,String type)
    {
        this.username=username;
        this.title=title;
        //null for fresh download but because we are not changing default behaviour so we put "Scrom" to identify that entry is for scrom
        this.filepath=filepath;

        //it will be that block which contain data i.e comp.id
        this.videoId=videoId;

        this.size=0;
        this.duration=0;

        this.url_high_quality="";
        this.url_low_quality="";
        this.url_youtube="";
        this.watched=WatchedState.UNWATCHED;
        this.downloaded=DownloadedState.DOWNLOADED;
        this.dmId=-1;

        //it will be root course .courseId.
        this.eid=eid;

        //it will be chapter name
        this.chapter=chapter;

        //it will be content type
        this.type=type;

        //it will be section name
        this.section=section;

        this.lastPlayedOffset=0;

        //lms url or unit url
        this.url="http://dummyurl";

        this.isCourseActive=1;
        this.isVideoForWebOnly=false;

        //downloadedOn it will be null for fresh download and set only for update case
        this.downloadedOn=System.currentTimeMillis();
    }
    public void setDownloadEntryForScrom(String username,String  title,String  filepath,String  videoId,
                                         String  url,String  eid,String  chapter,
                                         String  section,Long  downloadedOn)
    {
        this.username=username;
        this.title=title;
        //null for fresh download but because we are not changing default behaviour so we put "Scrom" to identify that entry is for scrom
        this.filepath=filepath;

        //it will be that block which contain data i.e comp.id
        this.videoId=videoId;

        this.size=0;
        this.duration=0;

        this.url_high_quality="";
        this.url_low_quality="";
        this.url_youtube="";
        this.watched=WatchedState.UNWATCHED;
        this.downloaded=DownloadedState.DOWNLOADED;
        this.dmId=-1;

        //it will be root course .courseId.
        this.eid=eid;

        //it will be chapter name
        this.chapter=chapter;

        //it will be section name
        this.section=section;

        this.lastPlayedOffset=0;

        //lms url or unit url
        this.url="http://connect.theteacherapp.org/wp-content/uploads/2017/03/%E0%A4%85%E0%A4%AA%E0%A4%A8%E0%A5%80-%E0%A4%95%E0%A4%B9%E0%A4%BE%E0%A4%A8%E0%A5%80-%E0%A4%B9%E0%A4%AE%E0%A4%BE%E0%A4%B0%E0%A5%87-%E0%A4%B8%E0%A4%BE%E0%A4%A5-%E0%A4%AC%E0%A4%BE%E0%A4%9F%E0%A5%87%E0%A4%82_8385993839_1488535120_VID-20170303-WA0039.mp4";//url;

        this.isCourseActive=1;
        this.isVideoForWebOnly=false;

        //downloadedOn it will be null for fresh download and set only for update case
        this.downloadedOn=System.currentTimeMillis();
    }
    public void setDownloadEntryForPost(long contentId, String category_id, String category_name, Post post)
    {
        String download_url="";
        //find the downloaded obj
        if(post.getFilter()!=null && post.getFilter().size()>0)
        {
            for (CustomFilter item:post.getFilter())
            {
                if(item==null || TextUtils.isEmpty(item.getName()))
                    continue;

                if(item.getName().toLowerCase().equals(String.valueOf(MxFilterType.MX_VIDEODOWNLOAD).toLowerCase())
                        && item.getChoices()!=null && item.getChoices().length > 0)
                {
                    download_url=item.getChoices()[0];
                    break;
                }
            }
        }
        this.username= BrowserUtil.loginPrefs.getUsername();
        this.title=post.getTitle().getRendered();

        //this.filepath=filepath;

        //need to work on
        this.videoId=String.valueOf(post.getId());
        this.size=0;
        this.duration=0;

        this.url_high_quality=download_url;
        this.url_low_quality=download_url;
        this.url=download_url;
        this.url_youtube="";
        this.watched=WatchedState.UNWATCHED;
        this.downloaded=DownloadedState.DOWNLOADED;
        this.dmId=-1;

        //it will be category id,in which post lies
        this.eid=category_id;

        //it will be chapter name
        this.chapter=category_name;

        //it will be content type
        this.type= DownloadType.WP_VIDEO.name();

        this.content_id = contentId;

        //it will be section name
        this.section=post.getTitle().getRendered();

        this.lastPlayedOffset=0;

        //lms url or unit url
        this.url=download_url;

        this.isCourseActive=1;
        this.isVideoForWebOnly=false;

        //downloadedOn it will be null for fresh download and set only for update case
        this.downloadedOn=0;
    }

    public void setDownloadEntryForScrom(String username,String  title,String  filepath,String  videoId,
                                         String  url,String  eid,String  chapter,
                                         String  section,long downloadedOn ,String scormUploadedOn)
    {
        this.username=username;
        this.title=title;
        //null for fresh download but because we are not changing default behaviour so we put "Scrom" to identify that entry is for scrom
        this.filepath=filepath;

        //it will be that block which contain data i.e comp.id
        this.videoId=videoId;

        this.size=0;
        this.duration=0;

        this.url_high_quality="";
        this.url_low_quality="";
        this.url_youtube="";
        this.watched=WatchedState.UNWATCHED;
        this.downloaded=DownloadedState.DOWNLOADED;
        this.dmId=-1;

        //it will be root course .courseId.
        this.eid=eid;

        //it will be chapter name
        this.chapter=chapter;

        //it will be section name
        this.section=section;

        this.lastPlayedOffset=0;

        //lms url or unit url
        this.url="http://connect.theteacherapp.org/wp-content/uploads/2017/03/%E0%A4%85%E0%A4%AA%E0%A4%A8%E0%A5%80-%E0%A4%95%E0%A4%B9%E0%A4%BE%E0%A4%A8%E0%A5%80-%E0%A4%B9%E0%A4%AE%E0%A4%BE%E0%A4%B0%E0%A5%87-%E0%A4%B8%E0%A4%BE%E0%A4%A5-%E0%A4%AC%E0%A4%BE%E0%A4%9F%E0%A5%87%E0%A4%82_8385993839_1488535120_VID-20170303-WA0039.mp4";//url;

        this.isCourseActive=1;
        this.isVideoForWebOnly=false;

        //downloadedOn it will be null for fresh download and set only for update case
        this.downloadedOn=System.currentTimeMillis();
        this.scormUploadedOn=scormUploadedOn;
    }
}
