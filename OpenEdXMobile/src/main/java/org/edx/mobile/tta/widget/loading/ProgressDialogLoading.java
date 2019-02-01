package org.edx.mobile.tta.widget.loading;

import android.content.Context;


/**
 * Created by Arjun on 2018/3/14.
 */

public class ProgressDialogLoading implements ILoading {

    private DialogProgressLoading mPd;

    public ProgressDialogLoading(Context context) {
        mPd = new DialogProgressLoading(context);
    }

    @Override
    public void showLoading() {
        mPd.show();
    }

    @Override
    public void hideLoading() {
        mPd.dismiss();
    }

    @Override
    public void dismiss() {
        mPd.dismiss();
    }
}
