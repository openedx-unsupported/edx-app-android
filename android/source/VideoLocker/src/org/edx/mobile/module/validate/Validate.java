package org.edx.mobile.module.validate;

/**
 * Created by rohan on 1/23/15.
 */
public class Validate {

    /**
     * Validates if the given object is not null.
     * Throws {@link java.lang.IllegalArgumentException} if given object is null.
     * @param obj
     */
    public static void notNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Expected non-null but found null");
        }
    }

    /**
     * Validates if the given string is not null and not empty.
     * Throws {@link java.lang.IllegalArgumentException} if given string is null or empty.
     * @param string
     */
    public static void notEmpty(String string) {
        if (ValidationUtil.isNullOrEmpty(string)) {
            throw new IllegalArgumentException("Expected non-null but found null");
        }
    }
}
