package org.edx.mobile.tta.wordpress_client.model;
import java.io.Serializable;

/**
 * Created by JARVICE on 22-12-2017.
 */

public class WpAuthResponse{

    private static final String ERROR_INVALID_GRANT = "invalid_grant";

    public String access_token;
    public String token_type;
    public long expires_in;
    public String scope;
    public String error;
    public String refresh_token;

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
}
