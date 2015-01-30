package org.edx.mobile.http.cache;

import android.content.Context;
import org.apache.commons.io.IOUtils;
import org.edx.mobile.util.Sha1Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import org.edx.mobile.logger.Logger;

public class CacheManager {

    private File cacheFolder;
    protected final Logger logger = new Logger(getClass().getName());

    public CacheManager(Context context) {
        if (context == null) {
            logger.warn("Context must not be NULL");
        }
        cacheFolder = new File(context.getFilesDir(), "http-cache");
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
    }

    public boolean has(String url) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        String hash = Sha1Util.SHA1(url);
        File file = new File(cacheFolder, hash);
        return file.exists();
    }

    public void put(String url, String response)
            throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException {
        String hash = Sha1Util.SHA1(url);
        File file = new File(cacheFolder, hash);
        FileOutputStream out = new FileOutputStream(file);
        out.write(response.getBytes());
        out.close();
        logger.debug("Cache.put = " + hash);
    }

    public String get(String url) throws IOException, NoSuchAlgorithmException {
        String hash = Sha1Util.SHA1(url);
        File file = new File(cacheFolder, hash);
        
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
}
