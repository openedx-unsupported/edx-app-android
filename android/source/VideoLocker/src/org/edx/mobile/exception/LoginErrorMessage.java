package org.edx.mobile.exception;

import java.util.Locale;

/**
 * Created by rohan on 1/21/15.
 */
public class LoginErrorMessage {
    private String messageLine1;
    private String messageLine2;

    public LoginErrorMessage(String line1, String line2) {
        this.messageLine1 = line1;
        this.messageLine2 = line2;
    }

    public String getMessageLine1() {
        return messageLine1;
    }

    public String getMessageLine2() {
        return messageLine2;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s. %s", messageLine1, messageLine2);
    }
}
