package org.edx.mobile.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by aleffert on 3/31/16.
 */
public class UrlUtil {

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
}
