package org.edx.mobile.authentication;

import org.edx.mobile.model.api.ProfileModel;

import java.io.Serializable;


/**
 * This class represents response of Authentication call to server.
 * 
 */
public class AuthResponse implements Serializable {

    private static final String ERROR_INVALID_GRANT = "invalid_grant";
    
    public String access_token;
    public String token_type;
    public long expires_in;
    public String scope;
    public String error;
    public String refresh_token;
    
    // non-api field
    public ProfileModel profile;
    
    @Override
    public String toString() {
        return String.format(
                "access_token=%s; token_type=%s; expires_in=%d; scope=%s; refresh_token=%s",
                access_token, token_type, expires_in, scope, refresh_token);
    }
    
    public boolean isSuccess() {
        return (error == null && access_token != null);
    }
    
    /**
     * Returns true if the response indicates that account is inactive.
     * @return
     */
    public boolean isAccountGrantError() {
        return (error != null && error.equalsIgnoreCase(ERROR_INVALID_GRANT));
    }

    /**
     * Returns true if this object contains a valid profile data.
     * @return
     */
    public boolean hasValidProfile() {
        return (profile != null
                && profile.email != null);
    }
}
