package org.edx.mobile.module.analytics;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.util.JavaUtil;

import java.util.Arrays;
import java.util.Map;

import static org.edx.mobile.util.JavaUtil.truncateString;

/**
 * Utility class that defines a specific format for an analytics event that we deliver to Firebase.
 */
public class FirebaseEvent {
    private static int EVENT_NAME_MAX_CHARS = 40;
    private static int PARAM_NAME_MAX_CHARS = 40;
    private static int PARAM_VALUE_MAX_CHARS = 100;
    private static String[] KEYS_TO_SKIP = {Analytics.Keys.URL, Analytics.Keys.TARGET_URL,
            Analytics.Keys.OPEN_BROWSER, Analytics.Keys.LABEL, Analytics.Keys.CATEGORY};

    @NonNull
    private String name;

    @NonNull
    private Bundle bundle;

    public FirebaseEvent(@NonNull String eventName) {
        setName(eventName);
        bundle = new Bundle();
        putString(Analytics.Keys.APP, Analytics.Values.APP_NAME);
        setCustomProperties();
    }

    public FirebaseEvent(@NonNull String eventName, @NonNull String biValue) {
        this(eventName);
        putBiValue(biValue);
    }

    public FirebaseEvent(@NonNull String eventName, @Nullable String videoId,
                         @NonNull String biValue) {
        this(eventName, biValue);
        if (videoId != null) {
            putModuleId(videoId);
        }
        putString(Analytics.Keys.CODE, Analytics.Values.MOBILE);
    }

    public FirebaseEvent(@NonNull String eventName, @Nullable String videoId,
                         @NonNull String biValue, @Nullable Double currentTime) {
        this(eventName, videoId, biValue);
        if (currentTime != null) {
            currentTime = JavaUtil.formatDoubleValue(currentTime, 3);
            putDouble(Analytics.Keys.CURRENT_TIME, currentTime);
        }
    }

    @NonNull
    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(@NonNull Bundle bundle) {
        this.bundle = bundle;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = applyRestrictions(name, EVENT_NAME_MAX_CHARS);
    }

    /**
     * Properties needed to be added with each analytics event will be done using this function.
     * Currently, we are adding Google Analytics' custom dimensions using it.
     */
    public void setCustomProperties() {
        // Device orientation dimension
        boolean isPortrait = MainApplication.instance().getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        putString(Analytics.Keys.DEVICE_ORIENTATION,
                (isPortrait ? Analytics.Values.PORTRAIT : Analytics.Values.LANDSCAPE));
    }

    /**
     * This function populates the event with some basic properties related to a course.
     *
     * @param courseId  A course's Id.
     * @param unitUrl   A course unit's url.
     * @param component A course's component.
     */
    public void setCourseContext(@Nullable String courseId, @Nullable String unitUrl,
                                 @Nullable String component) {
        if (courseId != null) {
            putCourseId(courseId);
        }

        if (unitUrl != null) {
            putString(Analytics.Keys.OPEN_BROWSER, unitUrl);
        }

        if (component != null) {
            putString(Analytics.Keys.COMPONENT, component);
        }
    }

    /**
     * Put the course's ID in the {@link #bundle}.
     *
     * @param courseId The course's ID.
     */
    public void putCourseId(@NonNull String courseId) {
        putString(Analytics.Keys.COURSE_ID, courseId);
    }

    /**
     * Only takes the last part of a module's ID separated by "@" character and puts
     * it in the {@link #bundle}.
     *
     * @param moduleOrBlockId The module's ID.
     */
    public void putModuleId(@NonNull String moduleOrBlockId) {
        // We just need to use the identifier at the end of the string.
        // e.g. 83d062b69f07415b8cf05dd5f33a8258 from block-v1:Microsoft+DEV212x+4T2016+type@video+block@83d062b69f07415b8cf05dd5f33a8258
        final String[] strings = moduleOrBlockId.split("@");
        putString(Analytics.Keys.MODULE_ID, strings[strings.length - 1]);
    }

    /**
     * Utility method to apply Firebase's restrictions the BI value before putting it in
     * {@link #bundle}.
     *
     * @param biValue The BI value.
     */
    public void putBiValue(@NonNull String biValue) {
        putString(Analytics.Keys.NAME, biValue);
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and String value
     * before putting them in {@link #bundle}.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void putString(@NonNull String key, @NonNull String value) {
        if (isSkippableKey(key)) return;
        bundle.putString(applyRestrictions(key, PARAM_NAME_MAX_CHARS),
                truncateString(value, PARAM_VALUE_MAX_CHARS));
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and Float value
     * before putting them in {@link #bundle}.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void putFloat(@NonNull String key, float value) {
        if (isSkippableKey(key)) return;
        bundle.putFloat(applyRestrictions(key, PARAM_NAME_MAX_CHARS), value);
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and Double value
     * before putting them in {@link #bundle}.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void putDouble(@NonNull String key, double value) {
        if (isSkippableKey(key)) return;
        bundle.putDouble(applyRestrictions(key, PARAM_NAME_MAX_CHARS), value);
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and Long value
     * before putting them in {@link #bundle}.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void putLong(@NonNull String key, long value) {
        if (isSkippableKey(key)) return;
        bundle.putLong(applyRestrictions(key, PARAM_NAME_MAX_CHARS), value);
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and Int value
     * before putting them in {@link #bundle}.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void putInt(@NonNull String key, int value) {
        if (isSkippableKey(key)) return;
        bundle.putInt(applyRestrictions(key, PARAM_NAME_MAX_CHARS), value);
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and Boolean value
     * before putting them in {@link #bundle}.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void putBoolean(@NonNull String key, boolean value) {
        if (isSkippableKey(key)) return;
        bundle.putBoolean(applyRestrictions(key, PARAM_NAME_MAX_CHARS), value);
    }

    /**
     * Utility method to apply Firebase's restrictions on String key and Double value
     * before putting them in {@link #bundle}.
     *
     * @param map The map containing keys and values.
     */
    public void putMap(@NonNull Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            if (isSkippableKey(key)) continue;
            bundle.putString(applyRestrictions(key, PARAM_NAME_MAX_CHARS),
                    applyRestrictions(entry.getValue(), PARAM_VALUE_MAX_CHARS));
        }
    }

    /**
     * Sets category and labels to BI events.
     *
     * @param category The category.
     * @param label    The label.
     */
    public void addCategoryToBiEvents(@NonNull String category, @Nullable String label) {
        putString(Analytics.Keys.CATEGORY, category);
        if (label != null) {
            putString(Analytics.Keys.LABEL, label);
        }
    }

    /**
     * Applies Firebase based restrictions on a string by truncating it, if it exceeds the
     * provided characters limit. Also, replaces the non-acceptable characters with underscore.
     *
     * @param string   The string to apply restrictions on.
     * @param maxChars The max number of characters allowed for the string.
     * @return The updated string.
     */
    @NonNull
    private String applyRestrictions(@NonNull String string, int maxChars) {
        // Truncate to given limit
        string = truncateString(string, maxChars);

        // Replace non-acceptable characters with underscore
        string = string.replaceAll("[:\\-\\s]+", "_");

        return string;
    }

    /**
     * Determines if we need to skip a key due to Firebase restrictions.
     *
     * @param key The key to check if its skippable.
     * @return <code>true</code> if a key is skippable, <code>false</code> otherwise.
     */
    private boolean isSkippableKey(@NonNull String key) {
        return Arrays.asList(KEYS_TO_SKIP).contains(key);
    }
}
