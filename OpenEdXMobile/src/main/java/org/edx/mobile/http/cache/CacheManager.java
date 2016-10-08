package org.edx.mobile.http.cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.IOUtils;
import org.edx.mobile.util.Sha1Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

@Singleton
public class CacheManager {
    private final Logger logger = new Logger(getClass().getName());

    @NonNull
    private final Context context;

    @Inject
    public CacheManager(@NonNull Context context) {
        this.context = context;
    }

    public boolean has(String url) {
        final File cacheDir = getCacheDir();
        if (cacheDir == null) return false;

        String hash = Sha1Util.SHA1(url);
        File file = new File(cacheDir, hash);
        return file.exists();
    }

    public void put(String url, String response) throws IOException {
        final File cacheDir = getCacheDir();
        if (cacheDir == null) throw new IOException("Cache directory not found");

        String hash = Sha1Util.SHA1(url);
        File file = new File(cacheDir, hash);
        FileOutputStream out = new FileOutputStream(file);
        out.write(response.getBytes());
        out.close();
        logger.debug("Cache.put = " + hash);
    }

    public String get(String url) throws IOException {
        final File cacheDir = getCacheDir();
        if (cacheDir == null) throw new IOException("Cache directory not found");

        String hash = Sha1Util.SHA1(url);
        File file = new File(cacheDir, hash);

        if (!file.exists()) {
            logger.debug("Cache.get failed, not cached");
            // not in cache
            return null;
        }

        FileInputStream in = new FileInputStream(file);
        String cache = IOUtils.toString(in, Charset.defaultCharset());
        in.close();
        logger.debug("Cache.get = " + hash);
        return cache;
    }

    @Nullable
    private File getCacheDir() {
        final File appDir = context.getFilesDir();
        if (appDir != null) {
            final File cacheDir = new File(appDir, "http-cache");
            cacheDir.mkdirs();
            return cacheDir;
        }
        return null;
    }
}
