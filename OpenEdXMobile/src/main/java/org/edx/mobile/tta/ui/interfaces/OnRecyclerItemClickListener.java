package org.edx.mobile.tta.ui.interfaces;

import android.view.View;

public interface OnRecyclerItemClickListener<T> {
    void onItemClick(View view, T item);
}
