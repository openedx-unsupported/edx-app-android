package org.edx.mobile.util;

import android.text.SpannableStringBuilder;

public class TextUtils {
    private TextUtils() {
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter The delimiter to use while joining.
     * @param tokens    An array of {@link CharSequence} to be joined using the delimiter.
     * @return A {@link CharSequence} joined using the provided delimiter.
     */
    public static CharSequence join(CharSequence delimiter, Iterable<CharSequence> tokens) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean firstTime = true;
        for (CharSequence token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb;
    }
}
