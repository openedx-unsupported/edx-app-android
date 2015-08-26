package org.edx.mobile.discussion;

import android.content.res.Resources;
import android.support.annotation.NonNull;
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
}
