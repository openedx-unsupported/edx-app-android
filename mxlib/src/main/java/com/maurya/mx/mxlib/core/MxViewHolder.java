package com.maurya.mx.mxlib.core;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

/**
 * Created by mukesh on 8/9/18.
 */

public class MxViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;
    public MxViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding=binding;
    }

    public ViewDataBinding getBinding() {
        return binding;
    }
}
