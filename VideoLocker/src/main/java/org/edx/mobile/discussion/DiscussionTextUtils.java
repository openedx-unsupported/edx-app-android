package org.edx.mobile.discussion;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.format.DateUtils;

import org.edx.mobile.R;
import org.edx.mobile.util.ResourceUtil;

import java.util.HashMap;

public abstract class DiscussionTextUtils {
    private DiscussionTextUtils() {
    }

    public static CharSequence getAuthorAttributionText(@NonNull final IAuthorData authorData, @NonNull final Resources resources) {
        final CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(
                authorData.getCreatedAt().getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE);

        final String authorLabel = authorData.getAuthorLabel();

        return trim(ResourceUtil.getFormattedString(resources, R.string.post_attribution, new HashMap<String, CharSequence>() {{
            put("time", formattedTime);
            put("author", authorData.getAuthor());
            put("author_label", null == authorLabel ? "" : authorLabel.toUpperCase(resources.getConfiguration().locale));
        }}));
    }

    public static CharSequence parseHtml(@NonNull String html) {
        // If the HTML contains a paragraph at the end, there will be blank lines following the text
        // Therefore, we need to trim the resulting CharSequence to remove those extra lines
        return trim(Html.fromHtml(html));
    }

    public static CharSequence trim(CharSequence s) {
        int start = 0, end = s.length();

        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }
}
