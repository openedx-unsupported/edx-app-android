package org.edx.mobile.module.db;

/**
 * This class defines databse structure and databse version number.
 * @author rohan
 *
 */
public final class DbStructure {

    public static final String NAME = "downloads.db";
    // Updated to Version 4 to add flag to indicate that video is only available for web
    // Updated to Version 5 to create a new table to record learning history for assessment
    // Updated to Version 6 to swap every occurrence of username field to its SHA1 hash
    // Updated to Version 7 to add a new field for HLS url encodings
    public static final int VERSION = 7;

    public static final class Table {
        public static final String DOWNLOADS = "downloads";
        public static final String ASSESSMENT = "assessment";

        //added by Arjun to store all TTA analytics offline and update them to web in batches
        public static final String ANALYTIC = "analytic";
        //added by Arjun to store all scrom resume state data //16-10-18
        public static final String TINCAN = "tincan";
    }

    public static final class Column {
        public static final String ID = "_id";
        public static final String USERNAME = "username";
        public static final String TITLE = "title";
        public static final String SIZE = "size";
        public static final String FILEPATH = "filepath";
        public static final String DURATION = "duration";
        public static final String WATCHED = "watched"; // watched, unwatched, partially watched
        public static final String DOWNLOADED = "downloaded"; // yes, no
        public static final String URL = "video_url";
        public static final String URL_HLS = "video_url_hls";
        public static final String URL_LOW_QUALITY = "video_url_low_quality";
        public static final String URL_HIGH_QUALITY = "video_url_high_quality";
        public static final String URL_YOUTUBE = "video_url_youtube";
        public static final String VIDEO_ID = "video_id";
        public static final String DM_ID = "download_manager_id";
        public static final String EID = "enrollment_id";
        public static final String CHAPTER = "chatper_name";
        public static final String SECTION = "section_name";
        // date in unix timestamp format
        public static final String DOWNLOADED_ON = "downloaded_on";
        public static final String LAST_PLAYED_OFFSET = "last_played_offset";
        public static final String IS_COURSE_ACTIVE = "is_course_active";
        public static final String UNIT_URL = "unit_url";
        public static final String VIDEO_FOR_WEB_ONLY = "video_for_web_only";

        //for differnciating content (wordpress,scrom,pdf)
        public static final String TYPE = "type";

        public static final String CONTENT_ID = "content_id";


        //table for assessment learning history
        public static final String ASSESSMENT_TB_ID = "_id";
        public static final String ASSESSMENT_TB_USERNAME = "username";
        public static final String ASSESSMENT_TB_UNIT_ID = "unit_id";
        public static final String ASSESSMENT_TB_UNIT_WATCHED = "unit_watched";

        //table for TTA Analytics
        public static final String ANALYTIC_TB_ID="_id";
        public static final String USER_ID="user_id";
        public static final String ACTION="action";
        public static final String METADATA= "metadata";
        public static final String PAGE="page";
        public static final String STATUS= "status";
        public static final String EVENT_DATE= "event_timestamp";
        public static final String NAV = "nav";
        public static final String ACTION_ID = "action_id";

        //table for TTA Tincan /scorm resume state
        public static final String COURSE_ID="course_id";
        public static final String RESUME_PAYLOAD="resume_payload";
        public static final String UNIT_ID = "unit_id";
    }
}
