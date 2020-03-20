package org.edx.mobile.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class PaginationData {
    private int count;
    private @SerializedName("num_pages") int numPages;

    private @Nullable String previous;
    private @Nullable String next;

    public PaginationData(int count, int numPages,  @Nullable String previous, @Nullable String next) {
        this.count = count;
        this.numPages = numPages;
        this.previous = previous;
        this.next = next;
    }

    @Nullable
    public String getPrevious() {
        return previous;
    }

    @Nullable
    public String getNext() {
        return next;
    }

    public int getCount() {
        return count;
    }
}
