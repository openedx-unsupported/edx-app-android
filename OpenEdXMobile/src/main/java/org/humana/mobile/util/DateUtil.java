package org.humana.mobile.util;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.humana.mobile.R;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.tta.exception.TaException;
import org.humana.mobile.tta.interfaces.OnResponseCallback;

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
        if (date == null) {
            return null;
        }

        java.util.Date parsedate = null;
        final ParsePosition parsePosition = new ParsePosition(0);
        try {
            parsedate = ISO8601Utils.parse(date, parsePosition);
            logger.debug("Parsed Data" + parsedate);
            return parsedate;

        } catch (ParseException e) {
            logger.error(e);
        }
        return parsedate;
    }

    /**
     * @return The current date and time in a ISO 8601 compliant format.
     */
    public static String getCurrentTimeStamp() {
        return ISO8601Utils.format(new Date(), true); // Find todays date
    }

    /**
     * This function returns course start date in the MMMM dd, yyyy format
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

    public static String getCurrentDateForServerLocal() {
        return getDateForServerLocal(System.currentTimeMillis());
    }

    public static String getDateForServerLocal(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            return sdf.format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentDateForServerGMT() {
        // return getDateForServerGMT(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));

        try {
            return sdf.format(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDisplayTime(String timeISO) {
        if (timeISO == null) {
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
    public static String getDateTime(Date date) {

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", locale);
        return outputFormat.format(date);

    }

    public static String getDisplayDate(long timestamp) {
        return getDisplayTime(new Date(timestamp));
    }

    public static String getDisplayDateTime(long timestamp) {
        return getDateTime(new Date(timestamp));
    }

    public static String getDisplayDateInMill(long timestamp) {
        return getDisplayTime(new Date(timestamp * 1000));
    }

    public static String getCalendarDate(long timestamp) {
        return getDisplayCalendarTime(new Date(timestamp));
    }

    public static String getDisplayCalendarTime(Date date) {

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd\nMMM", locale);
        return outputFormat.format(date);

    }

    public static String getDayMonth(long timestamp) {

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", locale);
        return outputFormat.format(new Date(timestamp));

    }

    public static String getHourMinute12(long timestamp) {

        Locale locale = new Locale("en");
        SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", locale);
        return outputFormat.format(new Date(timestamp));

    }

    public static void showDatePicker(Context context, long selectedTime,String title, OnResponseCallback<Long> callback) {
        Calendar cal = Calendar.getInstance();

        if (selectedTime > 0) {
            cal.setTimeInMillis(selectedTime);
        }
        DatePickerDialog dialog = new DatePickerDialog(context, R.style.DatePickerDialogStyle,
                (datePicker, i, i1, i2) -> {

            cal.set(Calendar.YEAR, i);
            cal.set(Calendar.MONTH, i1);
            cal.set(Calendar.DAY_OF_MONTH, i2);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            String str;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00 a", Locale.ENGLISH);
            str = df.format(cal.getTime());
            Date date = null;
            try {
                date = df.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long epoch = date.getTime();


//            cal.set(i, i1, i2);
            callback.onSuccess(epoch);

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(dialogInterface -> {
            callback.onFailure(new TaException("Date not selected"));
        });

        if (!title.equals("")) {
            dialog.setTitle(title);
            if (title.equals(context.getString(R.string.my_date))){
                dialog.setIcon(R.drawable.student_icon);
            }else {
                dialog.setIcon(R.drawable.teacher_icon);
            }
        }
        dialog.show();
    }
}