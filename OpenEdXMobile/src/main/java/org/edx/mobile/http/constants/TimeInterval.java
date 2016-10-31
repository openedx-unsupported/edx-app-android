package org.edx.mobile.http.constants;

/**
 * Constants for time intervals in seconds. These are meant to be used for defining the maximum ages
 * in the Cache-Control headers that are defined in Retrofit's HTTP API interfaces.
 */
public class TimeInterval {
    // Make this class non-instantiable
    private TimeInterval() {
        throw new UnsupportedOperationException();
    }

    /**
     * A constant value of one, representing one second. This is defined for consistency with all
     * the other units, and a convenience for clarification of the time format.
     */
    public static final int SECOND = 1;

    /**
     * One minute interval, represented in seconds.
     */
    public static final int MINUTE = SECOND * 60;

    /**
     * One hour interval, represented in seconds.
     */
    public static final int HOUR = MINUTE * 60;

    /**
     * One day interval, represented in seconds.
     */
    public static final int DAY = HOUR * 24;

    /**
     * One week interval, represented in seconds.
     */
    public static final int WEEK = DAY * 7;

    /**
     * One month (30 days) interval, represented in seconds.
     */
    public static final int MONTH = DAY * 30;

    /**
     * One year interval, represented in seconds.
     */
    public static final int YEAR = DAY * 365;
}
