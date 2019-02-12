package org.edx.mobile.tta.ui.base.mvvm;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.BR;
import org.edx.mobile.tta.ui.base.TaBaseActivity;

/**
 * Created by Arjun on 2018/3/14.
 */

public class BaseVMActivity extends TaBaseActivity {
    protected ViewDataBinding mBinding;
    protected BaseViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected ViewDataBinding binding(int layoutId, BaseViewModel viewModel) {
        mBinding = DataBindingUtil.setContentView(this, layoutId);
        this.mViewModel = viewModel;
        mBinding.setVariable(BR.viewModel, viewModel);
        return mBinding;
    }

    protected ViewDataBinding binding(int layoutId, BaseViewModel viewModel, Object store) {
        mBinding = DataBindingUtil.setContentView(this, layoutId);
        this.mViewModel = viewModel;
        mBinding.setVariable(BR.viewModel, viewModel);
        return mBinding;
    }

    public BaseViewModel getViewModel(){
        return mViewModel;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.onResume();
    }
}
