package org.edx.mobile.event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.ArrayMap;

import org.apache.commons.lang.time.FastDateFormat;
import org.edx.mobile.R;
import org.edx.mobile.util.ResourceUtil;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;
import java.util.Map;

/**
 * An event signifying that a new version of the app is
 * available on the app stores.
 */
public class NewVersionAvailableEvent {
    // Use FastDateFormat because it's thread-safe and caches instances.
    private static final Format dateFormat = FastDateFormat.getDateInstance(DateFormat.DEFAULT);

    private final Date lastSupportedDate;

    /**
     * Construct an instance of NewVersionAvailableEvent with the last
     * supported date unavailable.
     */
    public NewVersionAvailableEvent() {
        this(null);
    }

    /**
     * Construct an instance of NewVersionAvailableEvent with the
     * specified last supported date.
     */
    public NewVersionAvailableEvent(@Nullable Date lastSupportedDate) {
        this.lastSupportedDate = lastSupportedDate;
    }

    /**
     * @return The last supported date, or null if it's unknown.
     */
    @Nullable
    public Date getLastSupportedDate() {
        return lastSupportedDate;
    }

    /**
     * Resolve the notification string, and return it.
     *
     * @param context A Context to resolve the string
     * @return The notification string
     */
    @NonNull
    public CharSequence getNotificationString(@NonNull Context context) {
        @StringRes
        final int notificationStringRes;
        final Map<String, CharSequence> phraseMapping = new ArrayMap<>();
        if (lastSupportedDate == null) {
            notificationStringRes = R.string.banner_new_version_content;
        } else {
            notificationStringRes = R.string.banner_deprecated_content;
            phraseMapping.put("last_supported_date", dateFormat.format(lastSupportedDate));
        }
        phraseMapping.put("platform_name", context.getText(R.string.platform_name));
        return ResourceUtil.getFormattedString(context.getResources(),
                notificationStringRes, phraseMapping);
    }
}
