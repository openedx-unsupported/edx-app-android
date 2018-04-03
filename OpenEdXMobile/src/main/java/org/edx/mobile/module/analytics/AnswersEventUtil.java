package org.edx.mobile.module.analytics;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.answers.AnswersEvent;

import org.edx.mobile.base.MainApplication;

/**
 * Utility class that defines specific methods to pre-populate an analytics event that we deliver to Answers.
 */
public class AnswersEventUtil {
    /**
     * Set common properties for every Answers event.
     *
     * @param event The event to populate.
     */
    public static void setCustomProperties(@NonNull AnswersEvent event) {
        event.putCustomAttribute(Analytics.Keys.APP, Analytics.Values.APP_NAME);
        boolean isPortrait = MainApplication.instance().getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        event.putCustomAttribute(Analytics.Keys.DEVICE_ORIENTATION,
                (isPortrait ? Analytics.Values.PORTRAIT : Analytics.Values.LANDSCAPE));
    }

    /**
     * Adds category and label to provided BI events.
     *
     * @param event    The event to populate.
     * @param category The category.
     * @param label    The label.
     */
    public static void addCategoryToBiEvents(@NonNull AnswersEvent event,
                                             @NonNull String category, @Nullable String label) {
        event.putCustomAttribute(Analytics.Keys.CATEGORY, category);
        if (!TextUtils.isEmpty(label)) {
            event.putCustomAttribute(Analytics.Keys.LABEL, label);
        }
    }
}
