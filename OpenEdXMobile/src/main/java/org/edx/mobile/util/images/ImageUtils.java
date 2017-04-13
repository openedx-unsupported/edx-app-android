package org.edx.mobile.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.third_party.crop.CropUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    private static final Logger logger = new Logger(ImageUtils.class.getName());

    /**
     * Reads the exif rotation tag from the image present on given uri, applies the required rotation
     * on the image and creates another image independent of exif rotation tag.
     *
     * @param context  Context to create file in external directory.
     * @param imageUri Uri of image which needs to be rotated.
     * @return Uri of rotated image.
     */
    @Nullable
    public static Uri rotateImageAccordingToExifTag(@NonNull Context context, @NonNull Uri imageUri) {
        System.gc();
        final String imagePath = imageUri.getPath().toString();
        final int requiredRotation = CropUtil.getOrientationFromUri(imagePath);

        if (requiredRotation == 0) {
            return imageUri;
        }

        final File file;
        try {
            file = File.createTempFile(
                    new StringBuilder(32)
                            .append("JPEG_")
                            .append(new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()))
                            .append("_2").toString(), ".jpg",
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
        } catch (IOException e) {
            logger.error(e);
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(imagePath);
            final Matrix matrix = new Matrix();
            matrix.postRotate(requiredRotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            // Catch memory error for low memory devices and return null in fallback scenario
            logger.error(e);
            if (bitmap != null) {
                bitmap.recycle();
            }
            return null;
        }

        final FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            bitmap.recycle();
            System.gc();
            return Uri.fromFile(file);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }
}
