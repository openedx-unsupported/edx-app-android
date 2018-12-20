package org.edx.mobile.tta.ui.otp.view_model;

import android.databinding.ObservableField;

import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.reset_password.ResetPasswordActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

public class OtpViewModel extends BaseViewModel {

    public ObservableField<String> digit1 = new ObservableField<>("");
    public ObservableField<String> digit2 = new ObservableField<>("");
    public ObservableField<String> digit3 = new ObservableField<>("");
    public ObservableField<String> digit4 = new ObservableField<>("");

    public OtpViewModel(BaseVMActivity activity) {
        super(activity);
    }

    public void verify(){
        ActivityUtil.gotoPage(mActivity, ResetPasswordActivity.class);
    }
}
