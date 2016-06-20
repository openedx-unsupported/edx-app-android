package org.edx.mobile.util;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import okio.Okio;

public class IOUtils {
    @NonNull
    public static String toString(@NonNull InputStream in, @NonNull Charset charset) throws IOException {
        return Okio.buffer(Okio.source(in)).readString(charset);
    }

    public static void copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        Okio.buffer(Okio.sink(out)).writeAll(Okio.source(in));
    }
}
