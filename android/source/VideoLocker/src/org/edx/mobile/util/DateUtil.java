package org.edx.mobile.util;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.edx.mobile.logger.Logger;

@SuppressLint("SimpleDateFormat")
public class DateUtil {

    private static final Logger logger = new Logger(DateUtil.class.getName());

    /*
     * Converting Date in string format to Date object and coverting the Current
     * Stamp
     */
    public static Date convertToDate(String date) {
        if(date==null){
            return null;
        }
        SimpleDateFormat parse_to_format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'");

        java.util.Date parsedate = null;
        try {
            parsedate = parse_to_format.parse(date);
            logger.debug("Parsed Data"+parsedate);
            return parsedate;

        } catch (ParseException e) {
            logger.error(e);
        }
        return parsedate;
    }

    /*
     * Function to convert mins to Hours:Mins from Mins
     */
    public static String convertMinsToHours(int minutes) {
        int hr = minutes / 60;
        int min = minutes % 60;
        return String.format("%02d:%02d", hr, min);
    }
    
    
    /**
     * 
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date
            return currentTimeStamp;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * Returns date in below format which is required for modification date
     * of last accessed subsection.
     * Format is "2014-11-20 22:10:54.569200+00:00".
     * @return
     */
    public static String getModificationDate() {
        try {
            // ISO 8601 international standard date format
            final String ISO8601 = "yyyy-MM-dd HH:mm:ss.SSSSSSZ";
            
            SimpleDateFormat dateFormat = new SimpleDateFormat(ISO8601);
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date
            
            // last timezone part must have colon in it
            int len = currentTimeStamp.length();
            String tz = currentTimeStamp.substring(len - 5);
            String timezone = tz.substring(0, 3) + ":" + tz.substring(3);
            
            String formattedTimestamp = currentTimeStamp.substring(0, len-5) + timezone;
            return formattedTimestamp;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     *  This function returns course start date in the MMMM dd, yyyy format
     */
    public static String formatCourseNotStartedDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date startDate = DateUtil.convertToDate(date);

            String formattedDate = dateFormat.format(startDate);
            return formattedDate;
        } catch (Exception e) {
            //This will be removed when the PR for log changes is merged with master
            logger.error(e);
            return null;
        }
    }

}
