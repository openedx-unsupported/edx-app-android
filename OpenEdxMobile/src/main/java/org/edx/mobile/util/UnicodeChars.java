package org.edx.mobile.util;

/**
 * A collection of Unicode characters that may be ambiguous when rendered directly as literals.
 */
public class UnicodeChars {
    // Make this class non-instantiable
    private UnicodeChars() {
        throw new UnsupportedOperationException();
    }

    /**
     * A variant of hyphen that doesn't allow line-breaking when read by a word processor.
     */
    public static final char NON_BREAKING_HYPHEN = '\u2011';
}
