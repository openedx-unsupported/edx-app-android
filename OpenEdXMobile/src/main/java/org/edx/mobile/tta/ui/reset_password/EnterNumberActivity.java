package org.edx.mobile.tta.ui.reset_password;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.reset_password.view_model.EnterNumberViewModel;

public class EnterNumberActivity extends BaseVMActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding(R.layout.t_activity_enter_number, new EnterNumberViewModel(this));
    }
}
