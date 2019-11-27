package org.edx.mobile.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

public class JavaUtil {
    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return null;
        }
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static Set<Long> primitiveLongToSet(long[] longs) {
        HashSet<Long> set = new HashSet<Long>(longs.length);
        for (long l : longs) {
            set.add(l);
        }
        return set;
    }

    public static String getMemorySize(long bytes) {
        if (bytes == 0) {
            return "0KB";
        }

        long s = bytes;
        int gb = (int) (s / (1024f * 1024f * 1024f));
        s = s % (1024 * 1024 * 1024);
        int mb = (int) (s / (1024f * 1024f));
        s = s % (1024 * 1024);
        int kb = (int) (s / 1024f);
        int b = (int) (s % 1024);

        return String.format("%d MB", mb);
    }

    /**
     * Converts and returns the provided duration into a readable format i.e. hh:mm:ss. Returns null
     * if duration is zero or negative.
     *
     * @param duration Video duration in seconds.
     * @return Formatted duration.
     */
    @Nullable
    public static String getDurationString(long duration) {
        if (duration <= 0) {
            return null;
        }
        long d = duration;
        int hours = (int) (d / 3600f);
        d = d % 3600;
        int mins = (int) (d / 60f);
        int secs = (int) (d % 60);
        if (hours <= 0) {
            return String.format("%02d:%02d", mins, secs);
        }
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    /**
     * This function returns decimals value for a Double upto provided places.
     *
     * @param value  The double value.
     * @param places The number of places after the decimal.
     * @return The formatted {@link Double}.
     */
    @NonNull
    public static Double formatDoubleValue(@NonNull Double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Truncates a string to the given characters limit.
     *
     * @param string   The string to truncate.
     * @param maxChars The max number of characters allowed for the string.
     * @return The truncated string.
     */
    @NonNull
    public static String truncateString(@NonNull String string, int maxChars) {
        if (string != null && string.length() > maxChars) {
            string = string.substring(0, maxChars);
        }
        return string;
    }
}
