package org.edx.mobile.util.images;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalImageChooserHelper {

    @Nullable
    private File outputFile;

    @Nullable
    private Uri outputFileUri;

    @NonNull
    public Intent createChooserIntent(final Context context) {
        onDestroy();
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
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final List<ResolveInfo> listCam = context.getPackageManager().queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Support using Gallery / File Manager
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Create a chooser using the gallery intent, and add all the camera intents
        final Intent chooserIntent = Intent.createChooser(galleryIntent, null);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        return chooserIntent;
    }

    @Nullable
    public Uri onActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // Some camera apps return null or empty data
            return data == null || data.getExtras() == null || data.getExtras().isEmpty() || MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction())
                    ? outputFileUri
                    : data.getData();
        }
        return null;
    }

    public void onDestroy() {
        if (null != outputFile) {
            outputFile.delete();
        }
    }
}
