package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.third_party.subscaleview.ImageSource;
import org.edx.mobile.user.SaveUriToFileTask;
import org.edx.mobile.view.custom.CropImageView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CropImageActivity extends BaseFragmentActivity {
    public static final String EXTRA_IMAGE_URI = "imageUri";
    public static final String EXTRA_CROP_RECT = "cropRect";
    public static final String EXTRA_FROM_CAMERA = "fromCamera";

    @Inject
    IEdxEnvironment environment;

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

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        task = new SaveUriToFileTask(this, (Uri) getIntent().getParcelableExtra(EXTRA_IMAGE_URI)) {
            @Override
            protected void onPostExecute(Uri imageUri) {
                super.onPostExecute(imageUri);
                final CropImageView imageView = (CropImageView) findViewById(R.id.image);
                imageView.setImage(ImageSource.uri(imageUri));

                findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (imageView.isReady()) {
                            setResult(Activity.RESULT_OK, new Intent()
                                    .putExtra(EXTRA_CROP_RECT, imageView.getCropRect())
                                    .putExtra(EXTRA_IMAGE_URI, imageUri)
                                    .putExtra(EXTRA_FROM_CAMERA, getIntent().getBooleanExtra(EXTRA_FROM_CAMERA, false))
                            );
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onException(Exception e) throws RuntimeException {
                throw new RuntimeException(e);
            }
        };
        task.execute();

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.PROFILE_CROP_PHOTO);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != task) {
            task.cancel(true);
        }
    }
}
