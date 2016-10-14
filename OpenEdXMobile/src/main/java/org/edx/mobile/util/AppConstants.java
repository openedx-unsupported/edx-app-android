package org.edx.mobile.util;

public enum AppConstants {
    ;

    @Deprecated // This is not a constant. Should move it to the activity and use savedInstanceState.
    public static boolean myVideosDeleteMode = false;
    @Deprecated // This is not a constant. Should move it to the activity and use savedInstanceState.
    public static boolean videoListDeleteMode = false;

    public static final String VIDEOLIST_BACK_PRESSED = "offline_video_back_pressed";

    public static final double MILLISECONDS_PER_SECOND = 1000.00;

    /**
     * This class defines the names of various directories which are used for
     * storing application data.
     */
    public static final class Directories {
        /**
         * The name of the directory which is used to store downloaded videos.
         */
        public static final String VIDEOS = "videos";
        /**
         * The name of the directory which is used to store subtitles of the
         * downloaded videos.
         */
        public static final String SUBTITLES = "subtitles";
    }
}
