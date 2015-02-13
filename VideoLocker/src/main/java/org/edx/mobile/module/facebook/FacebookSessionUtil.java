package org.edx.mobile.module.facebook;

import com.facebook.Session;

/**
 * Created by rohan on 2/12/15.
 */
public class FacebookSessionUtil {

    /**
     * Returns Facebook session accessToken if available, null otherwise.
     * @return
     */
    public static String getAccessToken() {
        Session session = Session.getActiveSession();
        if (session != null) {
            return session.getAccessToken();
        }
        else {
            return null;
        }
    }
}
