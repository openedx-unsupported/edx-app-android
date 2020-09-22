package org.edx.mobile.util;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.edx.mobile.logger.Logger;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

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
     * This function returns course date in the EEE, MMM dd, yyyy format
     */
    public static String formatCourseDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
            Date startDate = DateUtil.convertToDate(date);

            return dateFormat.format(startDate);
        } catch (Exception e) {
            logger.error(e);
        }
        return "";
    }

    /**
     * This function match the provide date with current date
     *
     * @return true if the provided date is today else false
     */
    public static boolean isDateToday(String date) {
        return convertToSimpleDate(date).equals(convertToSimpleDate(getCurrentTimeStamp()));
    }

    /**
     * This function compare the provide date with current date
     *
     * @return true if the provided date is past else false
     */
    public static boolean isPastDate(String date) {
        Date currentDate = new Date();
        Date pastDate = convertToDate(date);
        if (pastDate != null) {
            return pastDate.compareTo(currentDate) < 0;
        }
        return false;
    }

    /**
     * This function compare the provide date with current date
     *
     * @return true if the provided date is due else false
     */
    public static boolean isDueDate(String date) {
        Date currentDate = new Date();
        Date dueDate = convertToDate(date);
        if (dueDate != null) {
            return dueDate.compareTo(currentDate) > 0;
        }
        return false;
    }

    /**
     * This function returns Simple date in the yyyy-MM-dd format
     */
    public static String convertToSimpleDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = DateUtil.convertToDate(date);

            return dateFormat.format(startDate);
        } catch (Exception e) {
            logger.error(e);
        }
        return "";
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
}
