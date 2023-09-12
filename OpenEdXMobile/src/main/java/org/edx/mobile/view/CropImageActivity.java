package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.third_party.subscaleview.ImageSource;
import org.edx.mobile.util.NonNullObserver;
import org.edx.mobile.view.custom.CropImageView;
import org.edx.mobile.viewModel.ProfileViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CropImageActivity extends BaseFragmentActivity {
    public static final String EXTRA_IMAGE_URI = "imageUri";
    public static final String EXTRA_CROP_RECT = "cropRect";
    public static final String EXTRA_FROM_CAMERA = "fromCamera";

    @Inject
    IEdxEnvironment environment;

    ProfileViewModel profileViewModel;

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

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        initObservers();

        Uri uri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        profileViewModel.copyUriContentToFile(this, uri);

        findViewById(R.id.cancel).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.PROFILE_CROP_PHOTO);
    }

    private void initObservers() {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.getCroppedImageUri().observe(this, new NonNullObserver<>(photoUri -> {
            final CropImageView imageView = findViewById(R.id.image);
            imageView.setImage(ImageSource.uri(photoUri));

            findViewById(R.id.save).setOnClickListener(v -> {
                if (imageView.isReady()) {
                    setResult(Activity.RESULT_OK, new Intent()
                            .putExtra(EXTRA_CROP_RECT, imageView.getCropRect())
                            .putExtra(EXTRA_IMAGE_URI, photoUri)
                            .putExtra(EXTRA_FROM_CAMERA, getIntent().getBooleanExtra(EXTRA_FROM_CAMERA, false))
                    );
                    finish();
                }
            });
            return null;
        }));
    }
}
