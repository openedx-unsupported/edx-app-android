package org.edx.mobile.tta.ui.logistration.view_model;

import android.Manifest;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.model.authentication.SendOTPResponse;
import org.edx.mobile.tta.task.authentication.GenerateOtpTask;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.otp.OtpActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.PermissionsUtil;

public class RegisterViewModel extends BaseViewModel {

    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    public ObservableField<String> confirmPassword = new ObservableField<>("");
    public ObservableBoolean cellValid = new ObservableBoolean();
    public ObservableBoolean passValid = new ObservableBoolean();
    public ObservableBoolean confirmPassValid = new ObservableBoolean();
    public ObservableInt passDrawable = new ObservableInt();
    public ObservableInt confirmPassDrawable = new ObservableInt();
    public ObservableBoolean passToggleEnabled = new ObservableBoolean();
    public ObservableBoolean confirmPassToggleEnabled = new ObservableBoolean();

    private boolean passVisible = false;
    private boolean confirmPassVisible = false;

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
            if (numString.length() == 10 && numString.matches("[0-9]+")) {
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
            if (passString.length() >= 3 && passString.equals(confirmPassword.get())) {
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
            if (confirmPassString.length() >= 3 && confirmPassString.equals(password.get())) {
                passValid.set(true);
                confirmPassValid.set(true);
            } else {
                passValid.set(false);
                confirmPassValid.set(false);
            }
        }
    };

    public RegisterViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void register() {
        //check here for message read and receive for otp feature
        /*if (PermissionsUtil.checkPermissions(Manifest.permission.READ_SMS, mActivity) &&
                PermissionsUtil.checkPermissions(Manifest.permission.RECEIVE_SMS, mActivity)) {
            generateOTP();
        } else {
            mFragment.askForPermissions(new String[]{Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS},
                    PermissionsUtil.READ_SMS_PERMISSION_REQUEST);
        }*/
        generateOTP();
    }

    public void signIn() {
        BaseVMActivity activity = (BaseVMActivity) mActivity;
        if (activity.getViewModel() instanceof SigninRegisterViewModel) {
            SigninRegisterViewModel viewModel = (SigninRegisterViewModel) activity.getViewModel();
            viewModel.toggleTab();
        }
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

    public void generateOTP(){
        mActivity.showLoading();

        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_MOBILE_NUMBER, cellphone.get());

        final String access_token = mDataManager.getLoginPrefs().getSocialLoginAccessToken();
        final String backstore = mDataManager.getLoginPrefs().getSocialLoginProvider();
        boolean fromSocialNet = !TextUtils.isEmpty(access_token);
        if (fromSocialNet) {
            parameters.putString(Constants.KEY_ACCESS_TOKEN, access_token);
            parameters.putString(Constants.KEY_PROVIDER, backstore);
            parameters.putString(Constants.KEY_CLIENT_ID, mDataManager.getConfig().getOAuthClientId());
        }

        //adding version for otp handling
        if(mDataManager.getConfig().getSMSKey()!=null || !mDataManager.getConfig().getSMSKey().isEmpty()) {
            parameters.putString("version", "1");
            parameters.putString("sms_key", mDataManager.getConfig().getSMSKey());
        }

        new GenerateOtpTask(mActivity, parameters) {
            @Override
            protected void onSuccess(SendOTPResponse sendOTPResponse) throws Exception {
                super.onSuccess(sendOTPResponse);
                mActivity.hideLoading();

                if(sendOTPResponse.mobile_number().equals(cellphone.get())){

                    parameters.putString(Constants.KEY_PASSWORD, password.get());
                    parameters.putString(Constants.KEY_OTP_SOURCE, Constants.OTP_SOURCE_REGISTER);
                    ActivityUtil.gotoPage(mActivity, OtpActivity.class, parameters);

                }
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hideLoading();
                String errorMsg = "";
                try {
                    if (((HttpResponseStatusException) ex).getStatusCode() == 409) {
                        errorMsg = "An account with this number already exists. Please try again";
                    } else if (((HttpResponseStatusException) ex).getStatusCode() == 404) {
                        errorMsg = "Please enter a valid mobile number";
                    } else {
                        errorMsg = "Please try again after sometime,Server not responding";
                    }
                } catch (Exception exp) {
                    errorMsg = "Please try again after sometime,Server not responding";
                }

                mFragment.showErrorDialog("Registration failure", errorMsg);
            }
        }.execute();
    }
}
