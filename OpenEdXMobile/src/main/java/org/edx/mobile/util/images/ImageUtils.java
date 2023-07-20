package org.edx.mobile.util.images;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

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

    /**
     * Check the validity of the context to be used for image loading via Glide library.
     * <br>
     * It's necessary to avoid the possible exceptions/crashes which are discussed in LEARNER-3186
     * in detail.
     * <br>
     * Glide issues:
     * <ul>
     * <li> <a href="https://github.com/bumptech/glide/issues/1484">https://github.com/bumptech/glide/issues/1484</li>
     * <li> <a href="https://github.com/bumptech/glide/issues/803">https://github.com/bumptech/glide/issues/803</li>
     * </ul>
     * <p>
     * TODO: Revisit this validity in LEARNER-4118
     *
     * @param context
     * @return <code>true</code> if context is valid for Glide to load the required image,
     * <code>false</code> otherwise.
     */
    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    public static void animateIconSize(ImageView imageView, float targetScale) {
        float initialScaleX = imageView.getScaleX();

        ValueAnimator animator = ValueAnimator.ofFloat(initialScaleX, targetScale);
        animator.addUpdateListener(valueAnimator -> {
            float animatedScale = (float) valueAnimator.getAnimatedValue();
            imageView.setScaleX(animatedScale);
            imageView.setScaleY(animatedScale);
        });

        animator.setDuration(200); // Adjust the animation duration as needed
        animator.start();
    }

    public static Drawable rotateVectorDrawable(Context context, @DrawableRes int drawableResId, float rotationDegrees) {
        Drawable originalDrawable = VectorDrawableCompat.create(context.getResources(), drawableResId, context.getTheme());
        if (originalDrawable == null) {
            return null;
        }

        return new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                float centerX = getBounds().exactCenterX();
                float centerY = getBounds().exactCenterY();

                canvas.save();
                canvas.rotate(rotationDegrees, centerX, centerY);
                originalDrawable.setBounds(getBounds());
                originalDrawable.draw(canvas);
                canvas.restore();
            }

            @Override
            public void setAlpha(int alpha) {
                originalDrawable.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(android.graphics.ColorFilter colorFilter) {
                originalDrawable.setColorFilter(colorFilter);
            }

            @Override
            public int getOpacity() {
                return originalDrawable.getOpacity();
            }

            @Override
            protected void onBoundsChange(Rect bounds) {
                super.onBoundsChange(bounds);
                originalDrawable.setBounds(bounds);
            }
        };
    }
}
