package org.edx.mobile.tta.ui.login;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;

/**
 * Created by Arjun on 2018/6/20.
 */

public class LoginActivity extends BaseVMActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding(R.layout.login_t_activity, new LoginViewModel(this));
    }
}
