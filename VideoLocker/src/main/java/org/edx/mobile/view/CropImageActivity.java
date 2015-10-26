package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.edx.mobile.R;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class CropImageActivity extends Activity {
    public static final String EXTRA_IMAGE_URI = "imageUri";

    public static Intent newIntent(@NonNull Context context, @NonNull Uri imageUri) {
        return new Intent(context, CropImageActivity.class)
                .putExtra(EXTRA_IMAGE_URI, imageUri);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        final ImageViewTouch imageView = (ImageViewTouch) findViewById(R.id.image);
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
                        (float)resource.getIntrinsicWidth() / imageView.getWidth(),
                        (float)resource.getIntrinsicHeight() / imageView.getHeight());
                imageView.setImageDrawable(resource, null, minZoom, 10.0f);
                return true;
            }
        }).into(imageView);
    }
}
