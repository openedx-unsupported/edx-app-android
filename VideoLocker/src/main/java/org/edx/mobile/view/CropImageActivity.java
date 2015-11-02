package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.edx.mobile.R;
import org.edx.mobile.user.SaveUriToFileTask;
import org.edx.mobile.view.custom.CropImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import roboguice.activity.RoboActivity;

public class CropImageActivity extends RoboActivity {
    public static final String EXTRA_IMAGE_URI = "imageUri";
    public static final String EXTRA_CROP_RECT = "cropRect";
    public static final String EXTRA_FROM_CAMERA = "fromCamera";

    public static Intent newIntent(@NonNull Context context, @NonNull Uri imageUri, boolean isFromCamera) {
        return new Intent(context, CropImageActivity.class)
                .putExtra(EXTRA_IMAGE_URI, imageUri)
                .putExtra(EXTRA_FROM_CAMERA, isFromCamera);
    }

    @Nullable
    public static Uri getImageUriFromResult(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_IMAGE_URI);
    }

    @Nullable
    public static Rect getCropRectFromResult(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_CROP_RECT);
    }

    public static boolean isResultFromCamera(@NonNull Intent data) {
        return data.getBooleanExtra(EXTRA_FROM_CAMERA, false);
    }

    private SaveUriToFileTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_crop_image);
        final CropImageView imageView = (CropImageView) findViewById(R.id.image);
        imageView.setDisplayType(ImageViewTouchBase.DisplayType.NONE);

        task = new SaveUriToFileTask(this, (Uri) getIntent().getParcelableExtra(EXTRA_IMAGE_URI)) {
            @Override
            protected void onSuccess(final Uri imageUri) throws Exception {
                Glide.with(CropImageActivity.this)
                        .load(imageUri)
                        .dontAnimate()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // TODO: account for padding around crop circle
                                final float viewportWidth = (float) imageView.getWidth() - getResources().getDimensionPixelSize(R.dimen.widget_margin) * 2;
                                final float minZoom = Math.max(
                                        viewportWidth / resource.getIntrinsicWidth(),
                                        viewportWidth / resource.getIntrinsicHeight())
                                        / ((float) imageView.getWidth() / resource.getIntrinsicWidth());
                                imageView.setImageDrawable(resource, null, minZoom, minZoom * 3);
                                return true;
                            }
                        }).into(imageView);

                findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(Activity.RESULT_OK, new Intent()
                                        .putExtra(EXTRA_CROP_RECT, imageView.getCropRect())
                                        .putExtra(EXTRA_IMAGE_URI, imageUri)
                                        .putExtra(EXTRA_FROM_CAMERA, getIntent().getBooleanExtra(EXTRA_FROM_CAMERA, false))
                        );
                        finish();
                    }
                });
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
            }
        };
        task.execute();

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != task) {
            task.cancel(true);
        }
    }
}
