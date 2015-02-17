package org.edx.mobile.exception;

/**
 * Created by rohan on 1/12/15.
 */
public class LoginException extends Exception {

    private LoginErrorMessage loginErrorMessage;

    public LoginException(LoginErrorMessage loginErrorMessage) {
        this.loginErrorMessage = loginErrorMessage;
    }

    public LoginErrorMessage getLoginErrorMessage() {
        return loginErrorMessage;
    }
}
