package org.edx.mobile.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.third_party.crop.CropUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class SetAccountImageTask extends
        Task<Void> {

    @Inject
    private UserAPI userAPI;

    @NonNull
    private final String username;

    @NonNull
    private final Uri uri;

    @NonNull
    private final Rect cropRect;

    public SetAccountImageTask(@NonNull Context context, @NonNull String username, @NonNull Uri uri, @NonNull Rect cropRect) {
        super(context);
        this.username = username;
        this.uri = uri;
        this.cropRect = cropRect;
    }


    public Void call() throws Exception {
        final File cropped = crop();
        userAPI.setProfileImage(username, cropped);
        return null;
    }

    private File crop() throws IOException {
        final File file = new File(context.getExternalCacheDir(), "cropped-image.jpg");
        final Bitmap croppedImage = CropUtil.decodeRegionCrop(getContext(), uri, cropRect, 500, 500);
        try {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            }
        } finally {
            croppedImage.recycle();
        }

        CropUtil.copyExifRotation(
                CropUtil.getFromMediaUri(getContext(), getContext().getContentResolver(), uri),
                file
        );

        return file;
    }
}
