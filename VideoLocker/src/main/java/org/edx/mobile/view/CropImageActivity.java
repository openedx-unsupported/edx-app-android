package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImageView;

import org.edx.mobile.R;

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
        final CropImageView cropImageView = (CropImageView) findViewById(R.id.crop_image_view);
        cropImageView.setImageUri((Uri) getIntent().getParcelableExtra(EXTRA_IMAGE_URI));
        cropImageView.setCropShape(CropImageView.CropShape.OVAL);
        cropImageView.setAspectRatio(1, 1);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cropImageView.setGuidelines(2);
    }
}
