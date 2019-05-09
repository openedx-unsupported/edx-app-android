package org.edx.mobile.util;

import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
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

    public static InputStream toInputStream(String input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(String input, Charset encoding) {
        return new ByteArrayInputStream(input.getBytes(toCharset(encoding)));
    }

    public static Charset toCharset(Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    public static Charset toCharset(String charset) {
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }
}
