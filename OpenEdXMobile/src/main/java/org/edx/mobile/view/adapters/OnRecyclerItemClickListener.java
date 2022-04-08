package org.edx.mobile.view.adapters;

import android.view.View;

public interface OnRecyclerItemClickListener<T> {
    void onItemClick(View view, T item);
}
