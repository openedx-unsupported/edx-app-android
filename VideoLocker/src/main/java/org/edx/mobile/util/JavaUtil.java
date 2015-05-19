package org.edx.mobile.util;

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
        int gb = (int) (s / (1024f * 1024f * 1024f) );
        s = s % (1024 * 1024 * 1024) ;
        int mb = (int) (s / (1024f * 1024f) );
        s = s % (1024 * 1024) ;
        int kb = (int) (s / 1024f);
        int b = (int) (s % 1024);

        return String.format("%d MB", mb);
    }

    public static String getDurationString(long duration) {
        if (duration == 0) {
            return "00:00";
        }

        long d = (long)duration;
        int hours = (int) (d / 3600f);
        d = d % 3600;
        int mins = (int) (d / 60f);
        int secs = (int) (d % 60);
        if (hours <= 0) {
            return String.format("%02d:%02d", mins, secs);
        }
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

}
