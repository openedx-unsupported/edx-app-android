package org.edx.mobile.social;

import android.app.Activity;

import org.edx.mobile.social.facebook.FacebookAuth;
import org.edx.mobile.social.google.GoogleOauth2;
import org.edx.mobile.util.Config;

public class SocialFactory {

    public static final int TYPE_GOOGLE         = 100;
    public static final int TYPE_FACEBOOK       = 101;
    
    public static ISocial getInstance(Activity activity, int type) {
        if (type == TYPE_GOOGLE) {
            if (Config.getInstance().getThirdPartyTraffic().isGoogleEnabled()) {
                return new GoogleOauth2(activity);
            }
            else {
                return new ISocialEmptyImpl();
            }
        } else if (type == TYPE_FACEBOOK) {
            if (Config.getInstance().getThirdPartyTraffic().isFacebookEnabled()) {
                return new FacebookAuth(activity);
            }
            else {
                return new ISocialEmptyImpl();
            }
        } 
        return null;
    }
}
