package org.edx.mobile.tta.ui.logistration.view_model;


import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.text.Editable;
import android.text.TextWatcher;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.tta.task.authentication.LoginTask;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.dashboard.DashboardActivity;
import org.edx.mobile.tta.ui.reset_password.EnterNumberActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

/**
 * Created by Arjun on 2018/6/20.
 */

public class SigninViewModel extends BaseViewModel {
    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    public ObservableBoolean cellValid = new ObservableBoolean();
    public ObservableBoolean passValid = new ObservableBoolean();
    public ObservableInt passDrawable = new ObservableInt();
    public ObservableBoolean passToggleEnabled = new ObservableBoolean();

    private boolean passVisible = false;

    public TextWatcher cellWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            cellphone.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 10 && numString.matches("[0-9]+")){
                cellValid.set(true);
            } else {
                cellValid.set(false);
            }
        }
    };

    public TextWatcher passWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            password.set(s.toString());
            setPassDrawable();
            String passString = s.toString();
            if (passString.length() >= 3){
                passValid.set(true);
            } else {
                passValid.set(false);
            }
        }
    };

    public SigninViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void login() {
//        ActivityUtil.gotoPage(mActivity, DashboardActivity.class);
        mActivity.show();
        new LoginTask(mActivity, cellphone.get(), password.get()){
            @Override
            protected void onSuccess(AuthResponse authResponse) throws Exception {
                super.onSuccess(authResponse);
                mActivity.hide();
                ActivityUtil.gotoPage(mActivity, DashboardActivity.class);
                mActivity.finish();
            }

            @Override
            protected void onException(Exception ex) {
//                super.onException(ex);
                mActivity.hide();
                if (ex instanceof AuthException){
                    mFragment.showErrorDialog(mActivity.getString(R.string.login_error),
                            mActivity.getString(R.string.login_failed));
                }
            }
        }.execute();
    }

    public void changePassword(){
        ActivityUtil.gotoPage(mActivity, EnterNumberActivity.class);
    }

    public void register(){
        BaseVMActivity activity = (BaseVMActivity) mActivity;
        if (activity.getViewModel() instanceof SigninRegisterViewModel){
            SigninRegisterViewModel viewModel = (SigninRegisterViewModel) activity.getViewModel();
            viewModel.toggleTab();
        }
    }

    private void setPassDrawable(){
        if (password.get().length() > 0){
            passToggleEnabled.set(true);
            if (passVisible){
                passDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                passDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            passToggleEnabled.set(false);
        }
    }
}
