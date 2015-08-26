package org.edx.mobile.discussion;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;

import org.edx.mobile.R;

public abstract class DiscussionTextUtils {
    private DiscussionTextUtils() {
    }

    public static String getAuthorAttributionText(@NonNull IAuthorData authorData, @NonNull Resources resources) {
        String text = authorData.getAuthor() + " " + DateUtils.getRelativeTimeSpanString(
                authorData.getCreatedAt().getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE);

        if (authorData.getAuthorLabel() == PinnedAuthor.STAFF) {
            text += "  " + resources.getString(R.string.discussion_priviledged_author_label_staff);

        } else if (authorData.getAuthorLabel() == PinnedAuthor.COMMUNITY_TA) {
            text = "  " + resources.getString(R.string.discussion_priviledged_author_label_ta);
        }

        return text;
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
