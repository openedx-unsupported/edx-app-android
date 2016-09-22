package org.edx.mobile.util;

import java.nio.charset.Charset;

/**
 * Copied from java.nio.charset.StandardCharsets, which isn't available until API 19
 */
public enum StandardCharsets {
    ;

    /**
     * Eight-bit UCS Transformation Format
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
}
