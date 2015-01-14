package com.github.axet.vget.info;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.info.VGetParser.VideoContentFirst;
import com.github.axet.vget.info.VGetParser.VideoDownload;
import com.github.axet.vget.vhs.VimeoParser;
import com.github.axet.vget.vhs.YouTubeParser;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.ex.DownloadError;
import com.github.axet.wget.info.ex.DownloadInterruptedError;
import com.github.axet.wget.info.ex.DownloadRetry;

public class VideoInfo {

    // keep it in order hi->lo
    public enum VideoQuality {
        p3072, p2304, p1080, p720, p520, p480, p360, p270, p240, p224, p144
    }

    public enum States {
        QUEUE, EXTRACTING, EXTRACTING_DONE, DOWNLOADING, RETRYING, DONE, ERROR, STOP
    }

    // user friendly url (not direct video stream url)
    private URL web;

    private List<VideoDownload> videoDownloads;

    private VideoQuality vq;
    private DownloadInfo info;
    private String title;
    private URL icon;

    // states, three variables
    private States state;
    private Throwable exception;
    private int delay;

    /**
     * 
     * @param vq
     *            max video quality to download
     * @param web
     *            user firendly url
     * @param video
     *            video stream url
     * @param title
     *            video title
     */
    public VideoInfo(URL web) {
        this.setWeb(web);

        reset();
    }

    /**
     * check if we have call extract()
     * 
     * @return true - if extract() already been called
     */
    public boolean empty() {
        return info == null;
    }

    /**
     * reset videoinfo state. make it simialar as after calling constructor
     */
    public void reset() {
        setState(States.QUEUE);

        info = null;
        vq = null;
        title = null;
        icon = null;
        exception = null;
        delay = 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DownloadInfo getInfo() {
        return info;
    }

    public void setInfo(DownloadInfo info) {
        this.info = info;
    }

    /**
     * get current video quality. holds actual videoquality ready for download
     * 
     * @return videoquality of requested URL
     */
    public VideoQuality getVideoQuality() {
        return vq;
    }

    /**
     * 
     * @param vq
     *            video quality
     */
    public void setVideoQuality(VideoQuality vq) {
        this.vq = vq;
    }

    public URL getWeb() {
        return web;
    }

    public void setWeb(URL source) {
        this.web = source;
    }

    public void extract(VideoInfoUser user, AtomicBoolean stop, Runnable notify) {
        VGetParser ei = null;

        if (YouTubeParser.probe(web))
            ei = new YouTubeParser(web);

        if (VimeoParser.probe(web))
            ei = new VimeoParser(web);

        if (ei == null)
            throw new RuntimeException("unsupported web site");

        try {
            videoDownloads = ei.extract(this, stop, notify);
            getVideo(this, user, videoDownloads);

            info.setReferer(web);

            info.extract(stop, notify);
        } catch (DownloadInterruptedError e) {
            setState(States.STOP, e);

            throw e;
        } catch (RuntimeException e) {
            setState(States.ERROR, e);

            throw e;
        }
    }

    public void getVideo(VideoInfo vvi, VideoInfoUser user, List<VideoDownload> sNextVideoURL) {
        if (sNextVideoURL.size() == 0) {
            // rare error:
            //
            // The live recording you're trying to play is still being processed
            // and will be available soon. Sorry, please try again later.
            //
            // retry. since youtube may already rendrered propertly quality.
            throw new DownloadRetry("empty video download list," + " wait until youtube will process the video");
        }

        Collections.sort(sNextVideoURL, new VideoContentFirst());

        for (int i = 0; i < sNextVideoURL.size(); i++) {
            VideoDownload v = sNextVideoURL.get(i);

            boolean found = true;

            if (user.getUserQuality() != null)
                found &= user.getUserQuality().equals(v.vq);

            if (found) {
                vvi.setVideoQuality(v.vq);
                DownloadInfo info = new DownloadInfo(v.url);
                vvi.setInfo(info);
                return;
            }
        }

        // throw download stop if user choice not maximum quality and we have no
        // video rendered by youtube

        // customize exception
        if (user.getUserQuality() != null)
            throw new DownloadError("no video user quality found");

        throw new DownloadError("no video with required quality found,"
                + " increace VideoInfo.setVq to the maximum and retry download");
    }

    public States getState() {
        return state;
    }

    public void setState(States state) {
        this.state = state;
        this.exception = null;
        this.delay = 0;
    }

    public void setState(States state, Throwable e) {
        this.state = state;
        this.exception = e;
        this.delay = 0;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay, Throwable e) {
        this.delay = delay;
        this.exception = e;
        this.state = States.RETRYING;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public URL getIcon() {
        return icon;
    }

    public void setIcon(URL icon) {
        this.icon = icon;
    }

    public List<VideoDownload> getVideoDownloads() {
        return videoDownloads;
    }

}