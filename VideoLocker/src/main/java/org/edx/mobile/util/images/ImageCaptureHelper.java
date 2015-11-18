package org.edx.mobile.util.images;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
        outputFileUri = Uri.fromFile(outputFile);

        // Support using Camera
        final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
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
