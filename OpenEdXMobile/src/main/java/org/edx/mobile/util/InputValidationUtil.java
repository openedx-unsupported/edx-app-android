package org.edx.mobile.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidationUtil {

    public static boolean isValidEmail(String text) {
        if (text == null) 
            return false;

        Pattern p = Pattern
                .compile("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-+]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        // Match the given string with the pattern
        Matcher m = p.matcher(text);
        // check whether match is found
        return m.matches();
    }
}
