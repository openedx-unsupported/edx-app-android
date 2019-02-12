package org.edx.mobile.tta.data.model.authentication;

public class VerifyOTPResponse {
    int id;
    String mobile_number;

    public String mobile_number() {
        return mobile_number;
    }
    public int id() {
        return id;
    }
}
