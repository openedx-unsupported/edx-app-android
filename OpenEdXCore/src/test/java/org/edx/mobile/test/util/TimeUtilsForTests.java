package org.edx.mobile.test.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtilsForTests {
    private static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.US);

    // Define a constant time zone to get predictable test parameters.
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("UTC");

    static {
        dateFormat.setTimeZone(DEFAULT_TIME_ZONE);
    }

    // Define a constant time to get predictable test parameters.
    public static final long DEFAULT_TIME = getMillis("2016-1-1 00:00:00");

    private TimeUtilsForTests() {
    }

    /**
     * Converts a given time stamp to milliseconds.
     *
     * @param timeStamp A date and time of the format {@link #DEFAULT_DATE_FORMAT}.
     * @return Milliseconds value of the give timeStamp.
     */
    public static long getMillis(String timeStamp) {
        Date date;
        try {
            date = dateFormat.parse(timeStamp);
        } catch (ParseException e) {
            return -1L;
        }
        return date.getTime();
    }
}
