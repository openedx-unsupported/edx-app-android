package org.edx.mobile.util;

public enum AppConstants {
    ;

    @Deprecated // This is not a constant. Should move it to the activity and use savedInstanceState.
    public static boolean videoListDeleteMode = false;

    public static final double MILLISECONDS_PER_SECOND = 1000.00;
    // A rating value to mark user has given rating and didn't give review/feedback so don't ask for rating again till next version
    public static final float APP_ZERO_RATING = 0.0f;
    // Threshold value to consider if the user hasn't rated the app
    public static final float APP_NOT_RATED_THRESHOLD = -1.0f;
    // Threshold value to consider if the user has given negative rating
    public static final float APP_NEGATIVE_RATING_THRESHOLD = 3.0f;
    // Minimum minor version changes required to ask negative raters to rate the app again
    public static final int MINOR_VERSIONS_DIFF_REQUIRED_FOR_NEGATIVE_RATERS = 2;
    // Minimum minor version changes required to show whats new screens
    public static final int MINOR_VERSIONS_DIFF_REQUIRED_FOR_WHATS_NEW = 1;
    // Threshold percentage of a web page's loading after which we are hiding the progress indicator
    public static final int PAGE_LOAD_THRESHOLD = 50;

    public static final String VIDEO_FORMAT_M3U8 = ".m3u8";
    public static final String VIDEO_FORMAT_MP4 = ".mp4";
    public static final String APP_URI_SCHEME = "org.edx.mobile.innerlinks://";

    public static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";
    public static final String BROWSER_PLAYSTORE_YOUTUBE_URI = "https://play.google.com/store/apps/details?id=" + YOUTUBE_PACKAGE_NAME;

    /**
     * This class defines all the Firebase constants related to the app.
     */
    public static final class FirebaseConstants {
        /**
         * Feature flag for whitelisted releases for course upgrade.
         */
        public static final String REV_934_WHITELISTED_RELEASES = "REV_934_Whitelisted_Releases";

        /**
         * Feature flag for value prop
         */
        public static final String VALUE_PROP_ENABLED = "VALUE_PROP_ENABLED";
    }

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
