package org.edx.mobile.exception;

/**
 * Created by rohan on 1/12/15.
 */
public class LoginException extends Exception {

    private String messageLine1;
    private String messageLine2;

    public LoginException(String line1, String line2) {
        this.messageLine1 = line1;
        this.messageLine2 = line2;
    }

    public String getMessageLine1() {
        return messageLine1;
    }

    public String getMessageLine2() {
        return messageLine2;
    }
}
