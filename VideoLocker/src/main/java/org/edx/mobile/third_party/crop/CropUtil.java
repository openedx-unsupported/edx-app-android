
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edx.mobile.third_party.crop;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Modified from https://github.com/jdamcd/android-crop which was itself modified from AOSP
 */
public class CropUtil {

    public static int getExifRotation(int rotation) {
        switch (rotation) {
            case 90:
                return ExifInterface.ORIENTATION_ROTATE_90;
            case 180:
                return ExifInterface.ORIENTATION_ROTATE_180;
            case 270:
                return ExifInterface.ORIENTATION_ROTATE_270;
            default:
                return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    public static void setExifOrientation(int rotation, @NonNull File destFile) throws IOException {
        ExifInterface exif = new ExifInterface(destFile.getAbsolutePath());
        if (rotation != 0) {
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(getExifRotation(rotation)));
            exif.saveAttributes();
        }
    }

    public static int getOrientation(@NonNull Context context, @NonNull Uri photoUri) {
        try (Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null)) {

            if (null == cursor || cursor.getCount() != 1) {
                return -1;
            }

            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    public static Bitmap decodeRegionCrop(@NonNull Context context, @NonNull Uri sourceUri, Rect rect, int outWidth, int outHeight, int rotation) throws IOException, OutOfMemoryError {
        Bitmap croppedImage;
        try (InputStream is = context.getContentResolver().openInputStream(sourceUri)) {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            final int width = decoder.getWidth();
            final int height = decoder.getHeight();

            if (rotation != 0) {
                // Adjust crop area to account for image rotation
                Matrix matrix = new Matrix();
                matrix.setRotate(-rotation);

                RectF adjusted = new RectF();
                matrix.mapRect(adjusted, new RectF(rect));

                // Adjust to account for origin at 0,0
                adjusted.offset(adjusted.left < 0 ? width : 0, adjusted.top < 0 ? height : 0);
                rect = new Rect((int) adjusted.left, (int) adjusted.top, (int) adjusted.right, (int) adjusted.bottom);
            }

            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
                if (rect.width() > outWidth || rect.height() > outHeight) {
                    Matrix matrix = new Matrix();
                    matrix.postScale((float) outWidth / rect.width(), (float) outHeight / rect.height());
                    croppedImage = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), matrix, true);
                }
            } catch (IllegalArgumentException e) {
                // Rethrow with some extra information
                throw new IllegalArgumentException("Rectangle " + rect + " is outside of the image ("
                        + width + "," + height + "," + rotation + ")", e);
            }
        }
        return croppedImage;
    }

    public static void crop(@NonNull Context context, @NonNull Uri uri, @NonNull Rect cropRect, int width, int height, @NonNull File file) throws IOException {
        int rotation = getOrientation(context, uri);
        final Bitmap croppedImage = CropUtil.decodeRegionCrop(context, uri, cropRect, width, height, rotation);
        try {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            }
        } finally {
            croppedImage.recycle();
        }

        CropUtil.setExifOrientation(
                rotation,
                file
        );

    }
}
