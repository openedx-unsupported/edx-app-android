package org.edx.mobile.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.edx.mobile.logger.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class UrlUtil {
    public static final String QUERY_PARAM_SEARCH = "search_query";
    public static final String QUERY_PARAM_SUBJECT = "subject";

    // Resolves a URL against a base URL. If the url is already absolute,
    // it just returns it. If the URL is relative it will resolve it against the base URL
    public static String makeAbsolute(String url, String base) {
        if (url == null || base == null) {
            return null;
        }
        try {
            URI baseUri = new URI(base);
            URI result = baseUri.resolve(url);
            return result.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds a valid URL with the given query params.
     *
     * @param logger      For logging purpose.
     * @param baseUrl     The base URL.
     * @param queryParams The query params to add in the URL.
     * @return URL String with query params added to it.
     */
    public static String buildUrlWithQueryParams(@NonNull Logger logger, @NonNull String baseUrl,
                                                 @NonNull Map<String, String> queryParams) {
        final Uri.Builder uriBuilder = Uri.parse(baseUrl).buildUpon();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        final String finalUrl = uriBuilder.build().toString();
        logger.debug("URL: " + finalUrl);
        return finalUrl;
    }
}
