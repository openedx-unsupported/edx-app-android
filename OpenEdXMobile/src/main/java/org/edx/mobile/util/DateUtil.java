package org.edx.mobile.util;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.edx.mobile.logger.Logger;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
    private static final Logger logger = new Logger(DateUtil.class.getName());

    /*
     * Converting Date in string format to Date object and converting the Current
     * Stamp
     */
    public static Date convertToDate(String date) {
        if(date==null){
            return null;
        }

        java.util.Date parsedate = null;
        final ParsePosition parsePosition = new ParsePosition(0);
        try {
            parsedate = ISO8601Utils.parse(date, parsePosition);
            logger.debug("Parsed Data"+parsedate);
            return parsedate;

        } catch (ParseException e) {
            logger.error(e);
        }
        return parsedate;
    }

    /**
     * @return The current date and time in a ISO 8601 compliant format.
     */
    public static String getCurrentTimeStamp(){
        return ISO8601Utils.format(new Date(), true); // Find todays date
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

    /**
     * Formats a date according to 'MMMM d' format.
     * Example output is 'February 21'.
     *
     * @param millis a point in time in UTC milliseconds
     * @return a string containing the formatted date.
     */
    @Nullable
    public static String formatDateWithNoYear(long millis) {
        try {
            return new SimpleDateFormat("MMMM d").format(millis);
        } catch (IllegalArgumentException e) {
            logger.error(e);
            return null;
        }
    }

    public static String getCurrentDateForServerLocal(){
        return getDateForServerLocal(System.currentTimeMillis());
    }

    public static String getDateForServerLocal(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try{
            return sdf.format(new Date(time));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentDateForServerGMT(){
        // return getDateForServerGMT(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));

        try{
            return sdf.format(new Date(System.currentTimeMillis()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getDisplayTime(String timeISO){
        if (timeISO == null){
            return null;
        }

        ParsePosition pos = new ParsePosition(0);
        Locale locale = new Locale("en");
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", locale);
        Date date = inputFormat.parse(timeISO, pos);
        return outputFormat.format(date);

        /*Date date = convertToDate(timeISO);
        if (date == null){
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        return day + getMonthInShort(month) + year;*/
    }

    public static String getDisplayTime(Date date) {

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", locale);
        return outputFormat.format(date);

    }

    public static String getDayMonth(long timestamp){

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", locale);
        return outputFormat.format(new Date(timestamp));

    }

    public static String getHourMinute12(long timestamp){

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", locale);
        return outputFormat.format(new Date(timestamp));

    }

    private static String getMonthInShort(int month) {
        switch (month){
            case 0:
                return "Jan";
            case 1:
                return "Feb";
            case 2:
                return "Mar";
            case 3:
                return "Apr";
            case 4:
                return "May";
            case 5:
                return "Jun";
            case 6:
                return "Jul";
            case 7:
                return "Aug";
            case 8:
                return "Sep";
            case 9:
                return "Oct";
            case 10:
                return "Nov";
            default:
                return "Dec";
        }
    }
}
