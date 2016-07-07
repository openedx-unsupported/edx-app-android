package org.edx.mobile.exception;

public class LoginException extends Exception {

    private LoginErrorMessage loginErrorMessage;

    public LoginException(LoginErrorMessage loginErrorMessage) {
        this.loginErrorMessage = loginErrorMessage;
    }

    public LoginErrorMessage getLoginErrorMessage() {
        return loginErrorMessage;
    }
}
