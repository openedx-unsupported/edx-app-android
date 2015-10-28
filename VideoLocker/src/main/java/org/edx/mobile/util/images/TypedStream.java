package org.edx.mobile.util.images;

import android.content.ContentResolver;
import android.net.Uri;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedOutput;

public class TypedStream implements TypedOutput {

    private final ContentResolver contentResolver;
    private final Uri uri;

    public TypedStream(ContentResolver contentResolver, Uri uri) {
        this.contentResolver = contentResolver;
        this.uri = uri;
    }

    @Override
    public String fileName() {
        return null;
    }

    @Override
    public String mimeType() {
        return contentResolver.getType(uri);
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        IOUtils.copy(contentResolver.openInputStream(uri), out);
    }
}