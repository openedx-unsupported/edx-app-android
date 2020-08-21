package org.edx.mobile.util;

import android.net.Uri;
import androidx.annotation.NonNull;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.links.WebViewLink;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UrlUtil {
    public static final String QUERY_PARAM_SEARCH = "q";
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

    /**
     * Get query parameters from the given URI.
     *
     * @param uri The URI.
     * @return The query params in the URI.
     */
    @NonNull
    public static Map<String, String> getQueryParams(@NonNull Uri uri) {
        final Map<String, String> paramsMap = new HashMap<>();
        final Set<String> paramNames = uri.getQueryParameterNames();
        for (String name : paramNames) {
            String value = uri.getQueryParameter(name);
            if (value != null) {
                if (name.equals(WebViewLink.Param.PATH_ID) &&
                        value.startsWith(WebViewLink.PATH_ID_COURSE_PREFIX)) {
                    // Our config already has this prefix in the URI, so we need to get rid of it here in the param's value
                    value = value.substring(WebViewLink.PATH_ID_COURSE_PREFIX.length()).trim();
                }
                paramsMap.put(name, value);
            }
        }
        return paramsMap;
    }

    /**
     * Utility function to remove the given query parameter from the URL
     * Ref: https://stackoverflow.com/a/56108097
     *
     * @param url        that needs to update
     * @param queryParam that needs to remove from the url
     */
    public static String removeQueryParameterFromURL(@NonNull String url, @NonNull String queryParam) {
        Uri uri = Uri.parse(url);
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        for (String param : params) {
            if (!queryParam.equals(param)) {
                newUri.appendQueryParameter(param, uri.getQueryParameter(param));
            }
        }
        return newUri.build().toString();
    }
}
