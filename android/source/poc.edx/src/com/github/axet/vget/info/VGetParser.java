package com.github.axet.vget.info;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.info.VideoInfo.VideoQuality;

public abstract class VGetParser {

    static public class VideoDownload {
        public VideoQuality vq;
        public URL url;

        public VideoDownload(VideoQuality vq, URL u) {
            this.vq = vq;
            this.url = u;
        }
    }

    static public class VideoContentFirst implements Comparator<VideoDownload> {

        @Override
        public int compare(VideoDownload o1, VideoDownload o2) {
            Integer i1 = o1.vq.ordinal();
            Integer i2 = o2.vq.ordinal();
            Integer ic = i1.compareTo(i2);

            return ic;
        }

    }

    public abstract List<VideoDownload> extract(final VideoInfo info, final AtomicBoolean stop, final Runnable notify);

}
