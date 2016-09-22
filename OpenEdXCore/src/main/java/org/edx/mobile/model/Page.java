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

    public Page(@NonNull PaginationData pagination, @NonNull List<T> results) {
        this.results = results;
        this.pagination = pagination;
    }

    @NonNull
    public List<T> getResults() {
        return results;
    }

    public final int getCount() {
        return pagination.getCount();
    }

    @Nullable
    public final String getNextUrl() {
        return pagination.getNext();
    }

    @Nullable
    public final String getPreviousUrl() {
        return pagination.getPrevious();
    }

    public final boolean hasNext() {
        return !TextUtils.isEmpty(pagination.getNext());
    }
}
