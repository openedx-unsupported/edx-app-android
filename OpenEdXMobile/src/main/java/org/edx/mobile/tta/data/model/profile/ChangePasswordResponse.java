package org.edx.mobile.tta.data.model.profile;

public class ChangePasswordResponse {

    String username;
    String old_password;
    String new_password;

    public String username() {
        return username;
    }

    public String old_password() {
        return old_password;
    }

    public String new_password() {
        return new_password;
    }

}
