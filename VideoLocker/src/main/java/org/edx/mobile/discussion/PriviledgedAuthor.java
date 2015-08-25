package org.edx.mobile.discussion;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.R;

public enum PriviledgedAuthor {

    @SerializedName("staff")
    STAFF (0),

    @SerializedName("community_ta")
    COMMUNITY_TA (1);

    private final int value;

    private PriviledgedAuthor(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getReadableText(Context context) {
        if (value == PriviledgedAuthor.COMMUNITY_TA.getValue()) {
            return context.getString(R.string.discussion_priviledged_author_label_ta);
        }

        return context.getString(R.string.discussion_priviledged_author_label_staff);
    }
}
