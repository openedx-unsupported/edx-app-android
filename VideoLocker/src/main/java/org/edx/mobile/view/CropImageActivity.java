package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.CropImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class CropImageActivity extends Activity {
    public static final String EXTRA_IMAGE_URI = "imageUri";
    public static final String EXTRA_CROP_RECT = "cropRect";

    public static Intent newIntent(@NonNull Context context, @NonNull Uri imageUri) {
        return new Intent(context, CropImageActivity.class)
                .putExtra(EXTRA_IMAGE_URI, imageUri);
    }

    @Nullable
    public static Uri getImageUriFromResult(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_IMAGE_URI);
    }

    @Nullable
    public static Rect getCropRectFromResult(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_CROP_RECT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_crop_image);
        final CropImageView imageView = (CropImageView) findViewById(R.id.image);
        imageView.setDisplayType(ImageViewTouchBase.DisplayType.NONE);
        final Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        Glide.with(this).load(imageUri).dontAnimate().listener(new RequestListener<Uri, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                final float minZoom = Math.max(
                        (float) resource.getIntrinsicWidth() / imageView.getWidth(),
                        (float) resource.getIntrinsicHeight() / imageView.getHeight());
                imageView.setImageDrawable(resource, null, minZoom, 10.0f);
                return true;
            }
        }).into(imageView);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, getIntent().putExtra(EXTRA_CROP_RECT, imageView.getCropRect()));
                finish();
            }
        });
    }
}
