package org.edx.mobile.util;

import android.content.Context;

import org.edx.mobile.logger.Logger;

import java.io.IOException;

/**
 * Common webview helper for any view that needs to use a webview.
 */
public class WebViewUtil {

    /**
     * Creates the intial StringBuffer used when an view uses a webview. Uses a common
     * css file.
     */
    public static StringBuilder getIntialWebviewBuffer(Context context, Logger logger) {
        StringBuilder buff = new StringBuilder();
        buff.append("<head>");
        buff.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        try {
            String cssFileContent = FileUtil.loadTextFileFromAssets(context,
                    "css/render-html-in-webview.css");
            buff.append("<style>");
            buff.append(cssFileContent);
            buff.append("</style>");
        } catch (IOException e) {
            logger.error(e);
        }
        buff.append("</head>");
        return buff;
    }
}
