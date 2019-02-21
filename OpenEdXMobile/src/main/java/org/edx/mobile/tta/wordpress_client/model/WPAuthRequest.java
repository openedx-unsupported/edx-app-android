package org.edx.mobile.tta.wordpress_client.model;

/**
 * Created by JARVICE on 26-12-2017.
 */

public class WPAuthRequest {
    String grant_type;
    String client_id;
    String username;
    String password;
    String client_secret;


    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    public void setClient_Id(String client_id) {
        this.client_id = client_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }
}
