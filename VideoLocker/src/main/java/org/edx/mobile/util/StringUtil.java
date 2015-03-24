package org.edx.mobile.util;

public class StringUtil {

    /**
     * Returns true if passed string is null or empty
     * @param text
     * @return
     */
    public static boolean isStringEmpty(String text) {
        if (text == null || text.isEmpty()){
            return true;
        }
        return false;
    }
}
