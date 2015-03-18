package org.edx.mobile.util;

/**
 * Created by rohan on 3/12/15.
 */
public class ConfigUtil {

    /**
     * Returns true if domain of the given URL is white-listed in the configuration,
     * false otherwise.
     * @param url
     * @return
     */
    public static boolean isWhiteListedURL(String url) {
        // check if this URL is a white-listed URL, anything outside the white-list is EXTERNAL LINK
        for (String domain : Config.getInstance().getDomainWhiteListConfig().getDomains()) {
            if (BrowserUtil.isUrlOfHost(url, domain)) {
                // this is white-listed URL
                return true;
            }
        }

        return false;
    }
}
