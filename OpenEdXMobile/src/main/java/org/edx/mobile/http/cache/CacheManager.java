package org.edx.mobile.http.cache;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.IOUtils;
import org.edx.mobile.util.Sha1Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * The cache manager for HTTP responses. The cache is stored on the filesystem, within the
 * application directory.
 *
 * @deprecated This is deprecated since with the transition to OkHttp, we're now relying on it's own
 * automatic caching mechanism. However, this is still being kept around as a read-only resource for
 * a while, being queried as a last resort by the CustomCacheQueryInterceptor, to facilitate the
 * transition without any user-facing issues. This may be removed later after a significant
 * percentage of the user base have upgraded to a version that uses the OkHttp API in the Courseware
 * module.
 */
@Singleton
@Deprecated
public class CacheManager {
    /**
     * The logger for this class.
     */
    private final Logger logger = new Logger(getClass().getName());

    /**
     * The application context.
     */
    @NonNull
    private final Context context;

    /**
     * Create a new instance of the cache manager.
     *
     * @param context The application context, to use for querying the app directory location.
     */
    @Inject
    public CacheManager(@NonNull final Context context) {
        this.context = context;
    }

    /**
     * Check whether there is a response body cached for the provided URL, and return the result.
     *
     * @param url The response URL.
     * @return True if there is a cached response body available, and false otherwise.
     */
    public boolean has(@NonNull final String url) {
        final File file = getFile(url);
        return file != null && file.exists();
    }

    /**
     * Get the cached response body for the provided URL.
     *
     * @param url The response URL.
     * @return The cached response body if available, and null otherwise.
     */
    @Nullable
    public String get(@NonNull final String url) {
        final File file = getFile(url);
        if (file != null) {
            try {
                return IOUtils.toString(file, Charset.defaultCharset());
            } catch (FileNotFoundException e) {
                // Cache is not available.
            } catch (IOException e) {
                logger.error(e, true);
            }
        }
        return null;
    }

    /**
     * Remove the cached response body for the provided URL.
     *
     * @param url The response URL.
     */
    public void remove(@NonNull final String url) {
        final File file = getFile(url);
        if (file != null) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    /**
     * Return the cache file for the provided URL.
     *
     * @param url The response URL.
     * @return The cache file, or null if the cache directory couldn't be created.
     */
    @Nullable
    private File getFile(@NonNull String url) {
        // Convert the URL to the original formats.
        url = convert(url);
        final File cacheDir = new File(context.getFilesDir(), "http-cache");
        if (cacheDir.mkdirs() || cacheDir.isDirectory()) {
            final String hash = Sha1Util.SHA1(url);
            return new File(cacheDir, hash);
        }
        return null;
    }

    /**
     * The regular expression pattern for the course structure GET URL, to be used for converting it
     * to the previous format.
     */
    private Pattern courseStructureGetUrlConversionPattern;

    /**
     * Convert the response URL from the new formats used in the Retrofit implementation, to the
     * previous formats used in the original Apache HTTP client implementation, which was what
     * populated this cache.
     *
     * @param url The URL to convert.
     * @return The converted URL.
     */
    @NonNull
    private String convert(@NonNull final String url) {
        /* The course structure URL was stripped of it's non-definitive query parameters before
         * being stored in the cache, to prevent it not becoming non-accessible after internal API
         * changes on the query parameters. The course ID parameter was retained, but not URL-
         * escaped.
         */
        if (courseStructureGetUrlConversionPattern == null) {
            final RoboInjector injector = RoboGuice.getInjector(context);
            final Config config = injector.getInstance(Config.class);
            String baseUrl = config.getApiHostURL();
            if (!baseUrl.endsWith("/")) {
                baseUrl += '/';
            }
            courseStructureGetUrlConversionPattern = Pattern.compile("^(" + baseUrl +
                    "api/courses/v1/blocks/\\?)(?:[^=&;]+?=[^=&;]+?[&;])*?(course_id=)([^=&;]+)");
        }
        final Matcher urlMatcher = courseStructureGetUrlConversionPattern.matcher(url);
        if (urlMatcher.matches()) {
            final StringBuffer urlConversionBuffer = new StringBuffer(url.length());
            urlMatcher.appendReplacement(urlConversionBuffer,
                    "$1$2" + Uri.decode(urlMatcher.group(3)));
            return urlConversionBuffer.toString();
        }
        return url;
    }
}
