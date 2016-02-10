package org.edx.mobile.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

public class Page<T> {
    @NonNull
    private List<T> results;

    @NonNull
    private PaginationData pagination;

    private final class PaginationData {
        int count, numPages;

        @Nullable
        String next, previous;
    }

    @NonNull
    public List<T> getResults() {
        return results;
    }

    public final int getCount() {
        return pagination.count;
    }

    @Nullable
    public final String getNextUrl() {
        return pagination.next;
    }

    @Nullable
    public final String getPreviousUrl() {
        return pagination.previous;
    }

    public final boolean hasNext() {
        return !TextUtils.isEmpty(pagination.next);
    }
}
