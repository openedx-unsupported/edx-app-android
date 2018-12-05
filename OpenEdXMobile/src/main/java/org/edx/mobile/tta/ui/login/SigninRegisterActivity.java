package org.edx.mobile.tta.ui.login;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.login.view_model.SigninRegisterViewModel;

/**
 * Created by Arjun on 2018/6/20.
 */

public class SigninRegisterActivity extends BaseVMActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding(R.layout.t_activity_signin_register, new SigninRegisterViewModel(this));
    }
}
