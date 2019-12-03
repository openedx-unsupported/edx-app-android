package org.humana.mobile.view.ProgressDialog_TA;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialog_TA extends ProgressDialog {
    public ProgressDialog_TA(Context context) {
        super(context);
    }
    @Override
    public void dismiss() {
        // do nothing
    }
    public void dismissManually() {
        super.dismiss();
    }
}
