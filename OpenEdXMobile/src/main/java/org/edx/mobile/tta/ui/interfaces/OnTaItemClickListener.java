package org.edx.mobile.tta.ui.interfaces;

import android.view.View;

public interface OnTaItemClickListener<T> {
    void onItemClick(View view, T item);
}
