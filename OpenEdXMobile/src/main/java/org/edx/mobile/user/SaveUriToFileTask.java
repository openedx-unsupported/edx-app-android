package org.edx.mobile.user;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.edx.mobile.task.Task;
import org.edx.mobile.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public abstract class SaveUriToFileTask extends
        Task<Uri> {

    @NonNull
    private final Uri uri;

    public SaveUriToFileTask(@NonNull Context context, @NonNull Uri uri) {
        super(context);
        this.uri = uri;
    }


    public Uri call() throws Exception {
        if ("file".equals(uri.getScheme())) {
            return uri; // URI already points to a file
        }

        {
            final Uri fileUri = getFileUriFromMediaStoreUri(getContext(), uri);
            if (null != fileUri) {
                return fileUri; // URI was successfully resolved to a file
            }
        }

        // URI does not point to a file; Download/copy it to a temporary file.
        final File outputFile = new File(context.getExternalCacheDir(), "cropped-image" + System.currentTimeMillis() + ".jpg");
        final InputStream inputStream = context.getContentResolver().openInputStream(uri);
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            try {
                IOUtils.copy(inputStream, fileOutputStream);
            } finally {
                fileOutputStream.close();
            }
        } finally {
            inputStream.close();
        }
        return Uri.fromFile(outputFile);
    }

    @Nullable
    private static Uri getFileUriFromMediaStoreUri(@NonNull Context context, @NonNull Uri photoUri) {
        final Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
        if (null == cursor) {
            return null;
        }
        try {
            if (cursor.moveToFirst() && cursor.getColumnCount() > 0) {
                final String data = cursor.getString(0);
                if (TextUtils.isEmpty(data)) {
                    return null;
                }
                return Uri.fromFile(new File(data));
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
