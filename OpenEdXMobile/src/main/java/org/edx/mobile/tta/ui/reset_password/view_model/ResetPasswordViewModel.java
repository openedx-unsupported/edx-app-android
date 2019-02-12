package org.edx.mobile.tta.ui.reset_password.view_model;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.model.authentication.ResetForgotedPasswordResponse;
import org.edx.mobile.tta.task.authentication.ResetForgotedPasswordTask;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

public class ResetPasswordViewModel extends BaseViewModel {

    public ObservableField<String> password = new ObservableField<>("");
    public ObservableField<String> confirmPassword = new ObservableField<>("");
    public ObservableBoolean passValid = new ObservableBoolean();
    public ObservableBoolean confirmPassValid = new ObservableBoolean();
    public ObservableInt passDrawable = new ObservableInt();
    public ObservableInt confirmPassDrawable = new ObservableInt();
    public ObservableBoolean passToggleEnabled = new ObservableBoolean();
    public ObservableBoolean confirmPassToggleEnabled = new ObservableBoolean();

    private boolean passVisible = false;
    private boolean confirmPassVisible = false;

    private String number;
    private String otpTransactionId;

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
            if (passString.length() >= 3 && passString.equals(confirmPassword.get())){
                passValid.set(true);
                confirmPassValid.set(true);
            } else {
                passValid.set(false);
                confirmPassValid.set(false);
            }
        }
    };

    public TextWatcher ConfirmPassWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            confirmPassword.set(s.toString());
            setConfirmPassDrawable();
            String confirmPassString = s.toString();
            if (confirmPassString.length() >= 3 && confirmPassString.equals(password.get())){
                passValid.set(true);
                confirmPassValid.set(true);
            } else {
                passValid.set(false);
                confirmPassValid.set(false);
            }
        }
    };

    public ResetPasswordViewModel(BaseVMActivity activity, String number, String otpTransactionId) {
        super(activity);
        this.number = number;
        this.otpTransactionId = otpTransactionId;
    }

    public void submit(){
        mActivity.showLoading();

        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_NEW_PASSWORD, password.get());
        parameters.putString(Constants.KEY_MOBILE_NUMBER, number);
        parameters.putString(Constants.KEY_OTP_TRANSACTION_ID, otpTransactionId);

        new ResetForgotedPasswordTask(mActivity, parameters){
            @Override
            protected void onSuccess(ResetForgotedPasswordResponse resetForgotedPasswordResponse) throws Exception {
                super.onSuccess(resetForgotedPasswordResponse);

                if(resetForgotedPasswordResponse.success() ) {

                    //rHelper.replaceFragment(R.id.verif_forgeted_password_otp_fragment_id, new ResetForgetedPasswordFagment(), getFragmentManager());
                    mActivity.showLongToast("Password successfully updated. Please login to continue");

                    ActivityUtil.gotoPage(mActivity, SigninRegisterActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mActivity.finish();
                }
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hideLoading();
                mActivity.showErrorDialog("Reset password failure", "Please try again later.");
            }
        }.execute();

    }

    private void setPassDrawable() {
        if (password.get().length() > 0) {
            passToggleEnabled.set(true);
            if (passVisible) {
                passDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                passDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            passToggleEnabled.set(false);
        }
    }

    private void setConfirmPassDrawable() {
        if (confirmPassword.get().length() > 0) {
            confirmPassToggleEnabled.set(true);
            if (confirmPassVisible) {
                confirmPassDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                confirmPassDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            confirmPassToggleEnabled.set(false);
        }
    }
}
