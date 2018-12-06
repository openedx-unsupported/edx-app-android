package org.edx.mobile.tta.ui.login.view_model;

import android.content.Context;
import android.databinding.ObservableField;

import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class RegisterViewModel extends BaseViewModel {

    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    public ObservableField<String> confirmPassword = new ObservableField<>("");

    public RegisterViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void register(){

    }

    public void signIn(){

    }
}
