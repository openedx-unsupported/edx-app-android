package org.edx.mobile.social;

import android.app.Activity;
import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.social.facebook.FacebookAuth;
import org.edx.mobile.social.google.GoogleOauth2;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;

public class SocialFactory {
    @Inject
    static Config config;

    //TODO - we should create a central place for application wide constants.
    public static enum SOCIAL_SOURCE_TYPE {
        TYPE_UNKNOWN(-1, "unknown"), TYPE_GOOGLE(100, "google-oauth2"), TYPE_FACEBOOK(101,"facebook");

        private int code;
        private String value;

        private SOCIAL_SOURCE_TYPE(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public static SOCIAL_SOURCE_TYPE fromString(String source){
            if ( "facebook".equalsIgnoreCase( source ) )
                return TYPE_FACEBOOK;
            if ( "google-oauth2".equalsIgnoreCase( source ) || "google".equalsIgnoreCase( source ) )
                return TYPE_GOOGLE;
            return TYPE_UNKNOWN;
        }
    }

    
    public static ISocial getInstance(Activity activity, SOCIAL_SOURCE_TYPE type, Config config) {
        if (type == SOCIAL_SOURCE_TYPE.TYPE_GOOGLE) {
            if ( isSocialFeatureEnabled(activity.getApplicationContext(), type, config) ) {
                return new GoogleOauth2(activity);
            }
            else {
                return new ISocialEmptyImpl();
            }
        } else if (type == SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK) {
            if ( isSocialFeatureEnabled(activity.getApplicationContext(),type, config)) {
                return new FacebookAuth(activity);
            }
            else {
                return new ISocialEmptyImpl();
            }
        } 
        return null;
    }

    public static boolean isSocialFeatureEnabled(Context context, SOCIAL_SOURCE_TYPE type, Config config) {
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(context, config);
        if ( isOnZeroRatedNetwork )
            return false;
        if (type == SOCIAL_SOURCE_TYPE.TYPE_GOOGLE) {
            return config.getGoogleConfig().isEnabled();
        } else if (type == SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK) {
            return config.getFacebookConfig().isEnabled();
        } else if (type == SOCIAL_SOURCE_TYPE.TYPE_UNKNOWN) {
            return config.getGoogleConfig().isEnabled()
                    || config.getFacebookConfig().isEnabled();
        }
        return false;
    }

}
