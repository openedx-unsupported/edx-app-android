package com.maurya.mx.mxlib.core;

import android.view.View;

/**
 * Created by mukesh on 27/4/18.
 */

public interface OnRecyclerItemClickListener<T> {
    void onItemClick(View view, T item);
}
