package org.edx.mobile.tta.ui.base.mvvm;

import android.arch.lifecycle.ViewModel;
import android.content.Context;

import org.edx.mobile.tta.data.DataManager;
import org.edx.mobile.tta.ui.base.TaBaseActivity;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

/**
 * Created by Arjun on 2018/3/11.
 */

public class BaseViewModel {
    protected TaBaseActivity mActivity;
    protected TaBaseFragment mFragment;
    protected DataManager mDataManager;

    public BaseViewModel(BaseVMActivity activity) {
        mActivity = activity;
        mDataManager = DataManager.getInstance(activity.getApplicationContext());

    }

    public BaseViewModel(Context context, TaBaseFragment fragment) {
        mActivity = (TaBaseActivity) context;
        mFragment = fragment;
        mDataManager = DataManager.getInstance(context);
    }

    public void onResume(){

    }

    public DataManager getDataManager(){
        return mDataManager;
    }

    public TaBaseActivity getActivity() {
        return mActivity;
    }
}
