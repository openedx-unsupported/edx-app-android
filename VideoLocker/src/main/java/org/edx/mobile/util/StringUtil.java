package org.edx.mobile.util;

public class StringUtil {

    /**
     * Returns false if passed string is null or empty
     * @param text
     * @return
     */
    public static boolean isStringEmpty(String text) {
        if (text == null || text.isEmpty()){
            return false;
        }
        return true;
    }
}
