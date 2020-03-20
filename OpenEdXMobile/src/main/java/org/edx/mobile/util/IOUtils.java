package org.edx.mobile.util;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import okio.Okio;

public class IOUtils {
    @NonNull
    public static String toString(@NonNull File file, @NonNull Charset charset) throws IOException {
        return Okio.buffer(Okio.source(file)).readString(charset);
    }

    public static void copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        Okio.buffer(Okio.sink(out)).writeAll(Okio.source(in));
    }
}
