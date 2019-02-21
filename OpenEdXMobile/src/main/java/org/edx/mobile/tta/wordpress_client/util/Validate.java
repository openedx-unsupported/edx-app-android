package org.edx.mobile.tta.wordpress_client.util;

import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Arjun Singh
 *         Created on 2016/01/08.
 */
public class Validate {

    public static boolean notNull(Object value) {
        return value != null;
    }

    public static void validateMapEntry(String key, Object value, Map<String, Object> map) {
        if (notNull(value)) {
            map.put(key, value);
        }
    }

    public static void validateMapStringEntry(String key, String value, Map<String, String> map) {
        if (!TextUtils.isEmpty(value)) {
            map.put(key, value);
        }
    }

    public static void validateMapLongEntry(String key, long value, Map<String, Object> map) {
        if (value != -1) {
            map.put(key, value);
        }
    }

    public static <T> void validateMapListEntry(String key, List<T> list, Map<String, Object> map) {
        if (list != null && list.size() > 0) {
            map.put(key, list);
        }
    }
}
