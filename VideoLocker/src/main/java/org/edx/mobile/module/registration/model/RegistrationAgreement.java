package org.edx.mobile.module.registration.model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.logger.Logger;

/**
 * Created by rohan on 2/12/15.
 */
public class RegistrationAgreement {

    private static Logger logger = new Logger(RegistrationAgreement.class);

    private @SerializedName("url")     String link;
    private @SerializedName("text")    String text;

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public boolean isInAppEULALink() {
        try {
            Uri uri = Uri.parse(link);
            if (uri.getScheme().equals("edxapp")
                    && uri.getHost().equals("show_eula")) {
                return true;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
        return false;
    }
}
