package org.edx.mobile.module.validate;

import com.facebook.internal.*;

import java.util.Collection;

/**
 * Created by rohan on 1/23/15.
 */
public class ValidationUtil {

    /**
     * Returns true if given object is null, false otherwise.
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj) {
        return (obj == null);
    }

    /**
     * Returns true if all given objects are not null, false otherwise.
     * @param objects
     * @return
     */
    public static boolean isNotNull(Object... objects) {
        if (objects != null) {
            for (Object obj : objects) {
                if (obj == null) {
                    // object is found null
                    return false;
                }
            }

            // all objects are non-null
            return true;
        }
        // parameters are null
        return false;
    }

    /**
     * Returns true if given string is null or empty, false otherwise.
     * @param string
     * @return
     */
    public static boolean isNullOrEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    /**
     * Returns true if givens strings are equal, false otherwise.
     * Comparison of strings is case-sensitive.
     * @param src
     * @param dest
     * @return
     */
    public static boolean matches(String src, String dest) {
        return (src.equals(dest));
    }
}
