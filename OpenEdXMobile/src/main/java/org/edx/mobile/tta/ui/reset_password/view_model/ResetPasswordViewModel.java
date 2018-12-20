package org.edx.mobile.tta.ui.reset_password.view_model;

import android.databinding.ObservableField;

import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class ResetPasswordViewModel extends BaseViewModel {

    public ObservableField<String> password = new ObservableField<>("");
    public ObservableField<String> confirm_password = new ObservableField<>("");

    public ResetPasswordViewModel(BaseVMActivity activity) {
        super(activity);
    }

    public void submit(){

    }
}
