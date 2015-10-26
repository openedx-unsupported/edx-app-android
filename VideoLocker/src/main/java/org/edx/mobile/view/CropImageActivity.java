package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

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
        final Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        Glide.with(this).load(imageUri).dontAnimate().into(imageView);
    }
}
