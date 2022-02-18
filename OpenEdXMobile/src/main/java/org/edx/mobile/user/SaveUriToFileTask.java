package org.edx.mobile.user;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.task.Task;
import org.edx.mobile.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class SaveUriToFileTask extends
        Task<Uri> {

    @NonNull
    private final Uri uri;

    public SaveUriToFileTask(@NonNull Context context, @NonNull Uri uri) {
        super(context);
        this.uri = uri;
    }

    @Override
    protected Uri doInBackground(Void... voids) {
        if ("file".equals(uri.getScheme())) {
            return uri; // URI already points to a file
        }

        {
            final Uri fileUri = getFileUriFromMediaStoreUri(context.get(), uri);
            if (null != fileUri) {
                return fileUri; // URI was successfully resolved to a file
            }
        }

        // URI does not point to a file; Download/copy it to a temporary file.
        final File outputFile = new File(context.get().getExternalCacheDir(), "cropped-image" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = context.get().getContentResolver().openInputStream(uri)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                IOUtils.copy(inputStream, fileOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            handleException(e);
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
