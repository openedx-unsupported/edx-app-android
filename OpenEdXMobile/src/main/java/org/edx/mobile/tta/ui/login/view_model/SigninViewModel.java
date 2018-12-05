package org.edx.mobile.tta.ui.login.view_model;


import android.content.Context;
import android.databinding.ObservableField;
import android.widget.Toast;

import org.edx.mobile.tta.data.DataManager;
import org.edx.mobile.tta.data.remote.NetworkObserver;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.login.model.LoginRequest;
import org.edx.mobile.tta.ui.login.model.LoginResponse;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.view.SplashActivity;

import javax.inject.Inject;

/**
 * Created by Arjun on 2018/6/20.
 */

public class SigninViewModel extends BaseViewModel {
    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");

    public SigninViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void login() {
        mDataManager.login(new LoginRequest(cellphone.get(), password.get()))
            .compose(mActivity.bindToLifecycle())
            .subscribe(new NetworkObserver<LoginResponse>(mActivity) {
                @Override
                protected void onHandleSuccess(LoginResponse loginResponse) {
                    super.onHandleSuccess(loginResponse);
                    Toast.makeText(mActivity,
                        "Login OK, token is" + loginResponse.getAuth_token(),
                        Toast.LENGTH_LONG).show();
                    ActivityUtil.gotoPage(mActivity, SplashActivity.class);
                }
            });
    }
}
