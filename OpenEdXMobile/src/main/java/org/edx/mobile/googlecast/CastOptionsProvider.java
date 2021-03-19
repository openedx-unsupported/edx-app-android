package org.edx.mobile.googlecast;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.ImageHints;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.google.android.gms.common.images.WebImage;

import java.util.List;

/**
 * *** DO NOT DELETE THIS FILE ***
 * App must implement the OptionsProvider interface to supply options needed to initialize the CastContext singleton.
 * Ref https://developers.google.com/cast/docs/android_sender/integrate#initialize_the_cast_context
 */
@SuppressWarnings("unused")
public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
        final NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setTargetActivityClassName(ExpandedControlsActivity.class.getName())
                .build();
        final CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName())
                .setImagePicker(new ImagePickerImpl())
                .setNotificationOptions(notificationOptions)
                .build();
        return new CastOptions.Builder()
                .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .setCastMediaOptions(mediaOptions)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

    private static class ImagePickerImpl extends ImagePicker {

        @Override
        public WebImage onPickImage(MediaMetadata mediaMetadata, @NonNull ImageHints imageHints) {
            if ((mediaMetadata == null) || !mediaMetadata.hasImages()) {
                return null;
            }
            return mediaMetadata.getImages().get(0);
        }
    }
}
