package org.humana.mobile.tta.ui.base.mvvm;

import android.content.Context;

import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.ui.base.TaBaseActivity;
import org.humana.mobile.tta.ui.base.TaBaseFragment;

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
