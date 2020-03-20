package org.edx.mobile.util;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.Date;
import java.util.TimeZone;

public class TimeZoneUtils {
    /**
     * Returns the region's abbreviation based on the time zone provided.
     *
     * @param timeZone
     * @return Region's abbreviation of the time zone.
     */
    public static String getTimeZoneAbbreviation(@NonNull TimeZone timeZone) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.icu.util.TimeZone tz = android.icu.util.TimeZone.getTimeZone(timeZone.getID());
            final String tzAbbr = tz.getDisplayName(timeZone.inDaylightTime(new Date()),
                    android.icu.util.TimeZone.SHORT);
            if (!TextUtils.isEmpty(tzAbbr) && !tzAbbr.contains("GMT")) {
                return tzAbbr;
            }
        }
        TimeZone tz = TimeZone.getTimeZone(timeZone.getID());
        // TODO: If provided time zone details are not available in device it will simply return
        // time zone in GMT format, in future we can do research either we can do ids to
        // abbreviation mapping and use them as alternate (considering day light saving factor in
        // calculation).
        return tz.getDisplayName(timeZone.inDaylightTime(new Date()), TimeZone.SHORT);
    }
}
