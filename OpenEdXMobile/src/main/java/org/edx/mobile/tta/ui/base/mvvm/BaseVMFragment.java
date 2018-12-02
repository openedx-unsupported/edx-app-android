package org.edx.mobile.tta.ui.base.mvvm;


import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.edx.mobile.BR;
import org.edx.mobile.tta.utils.RxV4Fragment;

/**
 * Created by Arjun on 2018/3/15.
 */

public class BaseVMFragment extends RxV4Fragment {
    protected ViewDataBinding mBinding;

    protected ViewDataBinding binding(LayoutInflater inflater, ViewGroup container, int layoutId, BaseViewModel viewModel) {
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        mBinding.setVariable(BR.viewModel, viewModel);

        return mBinding;
    }
}
