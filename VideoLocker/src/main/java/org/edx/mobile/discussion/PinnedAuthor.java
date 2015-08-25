package org.edx.mobile.discussion;

import com.google.gson.annotations.SerializedName;

public enum PinnedAuthor {

    @SerializedName("staff")
    STAFF (0),

    @SerializedName("community_ta")
    COMMUNITY_TA (1);

    private final int value;

    private PinnedAuthor(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
