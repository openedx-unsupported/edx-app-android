package org.edx.mobile.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.RoundedProfilePictureView;

public class SocialUtils {

    public static interface Values{

        public static final String NONE = "none";
        public static final String FACEBOOK = "Facebook";

    }

    public static enum SocialType {
        NONE,
        FACEBOOK
    }

    public static boolean isUriAvailable(PackageManager pm, Uri uri) {
        Intent test = new Intent(Intent.ACTION_VIEW, uri);
        return pm.resolveActivity(test, 0) != null;
    }

    public static Intent makeGroupLaunchIntent(Context context, String id, SocialType type){

        switch (type){

            case FACEBOOK:

                //If the group id is a full URL get the ID from the URL
                if (id.contains("/groups/")){
                    String tempID  = id.split("groups/")[1];

                    int eol = tempID.contains("/") ? tempID.indexOf("/") : tempID.length() - 1;
                    id = tempID.substring(0, eol);

                }

                PackageManager pm = context.getPackageManager();
                Uri uri;

                //Todo add preferential check for com.facebook.groups
                uri = Uri.parse("fb://group/" + id);
                if (!SocialUtils.isUriAvailable(pm, uri)){
                    uri = Uri.parse("https://m.facebook.com/groups/" + id);
                }
                return new Intent(Intent.ACTION_VIEW, uri);
        }

        return null;

    }

    /**
     * Generate a View for the users
     */
    public static View getAvatarView(Context context, View recycle, String id) {

        if (!TextUtils.isEmpty(id)) {
            RoundedProfilePictureView fbAvatarView;
            if (recycle != null) {
                fbAvatarView = (RoundedProfilePictureView) recycle;
                if (id.equals(fbAvatarView.getTag())) {
                    return fbAvatarView;
                }
            } else {
                fbAvatarView = new RoundedProfilePictureView(context);
                fbAvatarView.setRoundedCornerRadius(context.getResources().getDimension(R.dimen.avatar_image_radius_small));
            }

            fbAvatarView.setProfileId(id);
            fbAvatarView.setTag(id);

            return fbAvatarView;

        } else {

            return new RoundedProfilePictureView(context);

        }

    }

}
