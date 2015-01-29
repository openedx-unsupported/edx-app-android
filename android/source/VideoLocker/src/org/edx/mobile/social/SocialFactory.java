package org.edx.mobile.social;

import android.app.Activity;

import org.edx.mobile.social.facebook.FacebookAuth;
import org.edx.mobile.social.google.GoogleOauth2;

public class SocialFactory {

    public static final int TYPE_GOOGLE         = 100;
    public static final int TYPE_FACEBOOK       = 101;
    
    public static ISocial getInstance(Activity activity, int type) {
        if (type == TYPE_GOOGLE) {
            return new GoogleOauth2(activity);
        } else if (type == TYPE_FACEBOOK) {
            return new FacebookAuth(activity);
        } 
        return null;
    }
}
