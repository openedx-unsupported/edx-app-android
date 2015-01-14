package com.github.axet.vget.vhs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.info.VideoInfo.States;
import com.github.axet.vget.info.VideoInfo.VideoQuality;
import com.github.axet.wget.WGet;
import com.github.axet.wget.WGet.HtmlLoader;
import com.github.axet.wget.info.ex.DownloadError;
import com.google.gson.Gson;

public class VimeoParser extends VGetParser {

    URL source;

    public static class VimeoData {
        public VimeoRequest request;
        public VimeoVideo video;
    }

    public static class VimeoVideo {
        public Map<String, String> thumbs;
        public String title;
    }

    public static class VimeoRequest {
        public String signature;
        public String session;
        public long timestamp;
        public long expires;
        public VimeoFiles files;
    }

    public static class VimeoFiles {
        public ArrayList<String> codecs;
        public VidemoCodec h264;
    }

    public static class VidemoCodec {
        public VideoDownloadLink hd;
        public VideoDownloadLink sd;
        public VideoDownloadLink mobile;
    }

    public static class VideoDownloadLink {
        public String url;
        public int height;
        public int width;
        public String id;
        public int bitrate;
    }

    public VimeoParser(URL input) {
        this.source = input;
    }

    public static boolean probe(URL url) {
        return url.toString().contains("vimeo.com");
    }

    public static String extractId(URL url) {
        // standard web url. format: "https://vimeo.com/49243107" or
        // "http://vimeo.com/channels/staffpicks/49243107"
        {
            Pattern u = Pattern.compile("vimeo.com.*/(\\d+)");
            Matcher um = u.matcher(url.toString());

            if (um.find())
                return um.group(1);
        }
        // rss feed url. format:
        // "http://vimeo.com/moogaloop.swf?clip_id=49243107"
        {
            Pattern u = Pattern.compile("vimeo.com.*=(\\d+)");
            Matcher um = u.matcher(url.toString());

            if (um.find())
                return um.group(1);
        }
        return null;
    }

    @Override
    public List<VideoDownload> extract(final VideoInfo info, final AtomicBoolean stop, final Runnable notify) {
        List<VideoDownload> list = new ArrayList<VGetParser.VideoDownload>();

        try {
            String id;
            String clip;
            {
                id = extractId(info.getWeb());
                if (id == null) {
                    throw new DownloadError("unknown url");
                }
                clip = "http://vimeo.com/m/" + id;
            }

            URL url = new URL(clip);

            String html = WGet.getHtml(url, new HtmlLoader() {
                @Override
                public void notifyRetry(int delay, Throwable e) {
                    info.setDelay(delay, e);
                    notify.run();
                }

                @Override
                public void notifyDownloading() {
                    info.setState(States.EXTRACTING);
                    notify.run();
                }

                @Override
                public void notifyMoved() {
                    info.setState(States.RETRYING);
                    notify.run();
                }
            }, stop);

            String config;
            {
                Pattern u = Pattern.compile("data-config-url=\"([^\"]+)\"");
                Matcher um = u.matcher(html);
                if (!um.find()) {
                    throw new DownloadError("unknown config vimeo respond");
                }
                config = um.group(1);
            }

            config = StringEscapeUtils.unescapeHtml4(config);

            String htmlConfig = WGet.getHtml(new URL(config), new HtmlLoader() {
                @Override
                public void notifyRetry(int delay, Throwable e) {
                    info.setDelay(delay, e);
                    notify.run();
                }

                @Override
                public void notifyDownloading() {
                    info.setState(States.EXTRACTING);
                    notify.run();
                }

                @Override
                public void notifyMoved() {
                    info.setState(States.RETRYING);
                    notify.run();
                }
            }, stop);

            VimeoData data = new Gson().fromJson(htmlConfig, VimeoData.class);

            String icon = data.video.thumbs.values().iterator().next();

            info.setTitle(data.video.title);

            list.add(new VideoDownload(VideoQuality.p1080, new URL(data.request.files.h264.hd.url)));

            list.add(new VideoDownload(VideoQuality.p480, new URL(data.request.files.h264.sd.url)));

            info.setIcon(new URL(icon));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

}
