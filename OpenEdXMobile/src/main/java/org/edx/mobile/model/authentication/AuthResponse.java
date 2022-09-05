package org.edx.mobile.model.authentication;

import java.io.Serializable;

/**
 * This class represents response of Authentication call to server.
 */
public class AuthResponse implements Serializable {

    public String access_token;
    public String token_type;
    public long expires_in;
    public String scope;
    public String error;
    public String refresh_token;

    public boolean isSuccess() {
        return (error == null && access_token != null);
    }
}
