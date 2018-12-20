package org.edx.mobile.tta.ui.base.mvvm;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

public class BaseViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;
    public BaseViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding=binding;
    }

    public ViewDataBinding getBinding() {
        return binding;
    }

}
