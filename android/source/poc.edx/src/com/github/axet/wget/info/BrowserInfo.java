package com.github.axet.wget.info;

import java.net.URL;


/**
 * BrowserInfo - keep all information about browser
 * 
 * @author axet
 * 
 */
public class BrowserInfo {
    static public final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";

    private String userAgent = USER_AGENT;
    private URL referer;

    synchronized public String getUserAgent() {
        return userAgent;
    }

    synchronized public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    synchronized public URL getReferer() {
        return referer;
    }

    synchronized public void setReferer(URL referer) {
        this.referer = referer;
    }
}
