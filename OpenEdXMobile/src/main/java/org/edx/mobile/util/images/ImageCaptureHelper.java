package org.edx.mobile.util.images;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCaptureHelper {

    @Nullable
    private File outputFile;

    @Nullable
    private Uri outputFileUri;

    @NonNull
    public Intent createCaptureIntent(final Context context) {
        deleteTemporaryFile();
        try {
            outputFile = File.createTempFile(
                    "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_",
                    ".jpg",
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outputFileUri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    outputFile);
        } else {
            outputFileUri = Uri.fromFile(outputFile);
        }

        // Support using Camera
        final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        // Adding extras to open front camera by default. Officially, there's no intent that targets
        // the front-facing camera, its just a hack not all device camera apps support these extras.
        // TODO: For complete solution we can build a custom camera interface and add the functionality.
        intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);

        return intent;
    }

    @Nullable
    public Uri getImageUriFromResult() {
        return outputFileUri;
    }

    public void deleteTemporaryFile() {
        if (null != outputFile) {
            outputFile.delete();
        }
    }
}
