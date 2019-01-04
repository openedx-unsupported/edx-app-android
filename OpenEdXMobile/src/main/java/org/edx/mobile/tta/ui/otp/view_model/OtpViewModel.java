package org.edx.mobile.tta.ui.otp.view_model;

import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.task.authentication.LoginTask;
import org.edx.mobile.tta.task.authentication.OTPVerificationForgotedPasswordTask;
import org.edx.mobile.tta.task.authentication.RegisterTask;
import org.edx.mobile.tta.task.authentication.VerifyOtpTask;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.dashboard.DashboardActivity;
import org.edx.mobile.tta.ui.login.model.RegisterResponse;
import org.edx.mobile.tta.ui.otp.IMessageReceiver;
import org.edx.mobile.tta.ui.otp.IncomingSms;
import org.edx.mobile.tta.ui.otp.OTP_helper;
import org.edx.mobile.tta.ui.otp.model.VerifyOTPForgotedPasswordResponse;
import org.edx.mobile.tta.ui.otp.model.VerifyOTPResponse;
import org.edx.mobile.tta.ui.reset_password.ResetPasswordActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.images.ErrorUtils;

public class OtpViewModel extends BaseViewModel {

    public ObservableField<String> digit1 = new ObservableField<>("");
    public ObservableField<String> digit2 = new ObservableField<>("");
    public ObservableField<String> digit3 = new ObservableField<>("");
    public ObservableField<String> digit4 = new ObservableField<>("");
    public ObservableField<String> digit5 = new ObservableField<>("");
    public ObservableField<String> digit6 = new ObservableField<>("");

    public ObservableBoolean valid1 = new ObservableBoolean();
    public ObservableBoolean valid2 = new ObservableBoolean();
    public ObservableBoolean valid3 = new ObservableBoolean();
    public ObservableBoolean valid4 = new ObservableBoolean();
    public ObservableBoolean valid5 = new ObservableBoolean();
    public ObservableBoolean valid6 = new ObservableBoolean();

    private String otp = "";

    public IncomingSms incomingSms;

    private String number;

    private String password;

    private String otpSource;

    public TextWatcher watcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit1.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid1.set(true);
            } else {
                valid1.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit2.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid2.set(true);
            } else {
                valid2.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher3 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit3.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid3.set(true);
            } else {
                valid3.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher4 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit4.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid4.set(true);
            } else {
                valid4.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher5 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit5.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid5.set(true);
            } else {
                valid5.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher6 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit6.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid6.set(true);
            } else {
                valid6.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public OtpViewModel(BaseVMActivity activity, String mobile_number, String password, String otpSource) {
        super(activity);
        this.number = mobile_number;
        this.password = password;
        this.otpSource = otpSource;

        incomingSms =new IncomingSms(new IMessageReceiver() {
            @Override
            public void onMessage(String from, String text) {
                OTP_helper helper=new OTP_helper();
                if(helper.isValidSender(from))
                {
                    otp = helper.getOTPFromMesssageBody(text);

                    digit1.set(String.valueOf(otp.charAt(0)));
                    digit2.set(String.valueOf(otp.charAt(1)));
                    digit3.set(String.valueOf(otp.charAt(2)));
                    digit4.set(String.valueOf(otp.charAt(3)));
                    digit5.set(String.valueOf(otp.charAt(4)));
                    digit6.set(String.valueOf(otp.charAt(5)));

                    //Toast.makeText(ctx,text, Toast.LENGTH_LONG).show();
                    mActivity.unregisterReceiver(incomingSms);
                    //mFrag.incomingSms = null;
                }
            }
        });
        registerMessageListner();

    }

    public void verify(){
        mActivity.show();

        if (otpSource.equals(Constants.OTP_SOURCE_RESET_PASSWORD)) {
            verifyOtpForForgottenPassword();
        } else if (otpSource.equals(Constants.OTP_SOURCE_REGISTER)) {
            verifyOtp();
        }
    }

    private void verifyOtpForForgottenPassword(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_OTP, otp);
        parameters.putString(Constants.KEY_MOBILE_NUMBER, number);

        new OTPVerificationForgotedPasswordTask(mActivity, parameters){
            @Override
            protected void onSuccess(VerifyOTPForgotedPasswordResponse verifyOTPForgotedPasswordResponse) throws Exception {
                super.onSuccess(verifyOTPForgotedPasswordResponse);
                mActivity.hide();

                if(verifyOTPForgotedPasswordResponse.transaction_id() != null && !verifyOTPForgotedPasswordResponse.transaction_id().equals("")) {
                    String user_otp_transation_id = verifyOTPForgotedPasswordResponse.transaction_id();

                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_MOBILE_NUMBER, number);
                    parameters.putString(Constants.KEY_OTP_TRANSACTION_ID, user_otp_transation_id);

                    ActivityUtil.gotoPage(mActivity, ResetPasswordActivity.class, parameters);

                }
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hide();
                mActivity.showErrorDialog("OTP Verification failure", "Please enter OTP you receive.");
            }
        }.execute();
    }

    private void verifyOtp(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_OTP, otp);
        parameters.putString(Constants.KEY_MOBILE_NUMBER, number);

        new VerifyOtpTask(mActivity, parameters){
            @Override
            protected void onSuccess(VerifyOTPResponse verifyOTPResponse) throws Exception {
                super.onSuccess(verifyOTPResponse);
                register();
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hide();
                mActivity.showErrorDialog("OTP Verification failure", "Please enter a valid otp or try again.");
            }
        }.execute();
    }

    private void register() {
        new RegisterTask(mActivity, getRegisterMeBundle(),
                mDataManager.getLoginPrefs().getSocialLoginAccessToken(),
                SocialFactory.SOCIAL_SOURCE_TYPE.fromString(mDataManager.getLoginPrefs().getSocialLoginProvider())){

            @Override
            protected void onSuccess(RegisterResponse registerResponse) throws Exception {
                super.onSuccess(registerResponse);
                signIn();
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hide();
                mActivity.showErrorDialog("Registration failure", "Unable to register, try again later.");
            }
        }.execute();
    }

    private void signIn(){

        new LoginTask(mActivity, number, password){
            @Override
            protected void onSuccess(AuthResponse authResponse) throws Exception {
                super.onSuccess(authResponse);
                mActivity.hide();
                ActivityUtil.gotoPage(mActivity, DashboardActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mActivity.finish();
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hide();

                if (ex instanceof AuthException) {
                    mActivity.showErrorDialog(
                            mActivity.getString(R.string.login_error),
                            mActivity.getString(R.string.login_failed));
                } else {
                    super.onException(ex);
                    mActivity.showErrorDialog(null, ErrorUtils.getErrorMessage(ex, mActivity));
                }
            }
        }.execute();

    }

    private Bundle getRegisterMeBundle()
    {
        Bundle regiParameter=new Bundle();

        // set honor_code and terms_of_service to true
        regiParameter.putString(Constants.KEY_HONOR_CODE, "true");
        regiParameter.putString(Constants.KEY_TERMS_OF_SERVICE, "true");

        //set parameter required by social registration
        final String access_token = mDataManager.getLoginPrefs().getSocialLoginAccessToken();
        final String backstore = mDataManager.getLoginPrefs().getSocialLoginProvider();
        boolean fromSocialNet = !TextUtils.isEmpty(access_token);
        if (fromSocialNet) {
            regiParameter.putString(Constants.KEY_ACCESS_TOKEN, access_token);
            regiParameter.putString(Constants.KEY_PROVIDER, backstore);
            regiParameter.putString(Constants.KEY_CLIENT_ID, mDataManager.getConfig().getOAuthClientId());
        }
        regiParameter.putString(Constants.KEY_NAME, number);
        regiParameter.putString(Constants.KEY_USERNAME, number);
        regiParameter.putString(Constants.KEY_PASSWORD, password );
        regiParameter.putString(Constants.KEY_EMAIL, number + "@theteacherapp.org" );
        regiParameter.putString(Constants.KEY_STATE, "");

        return regiParameter;
    }

    private void registerMessageListner()
    {
        IntentFilter ifilter = new IntentFilter();
        // Use number higher than 999 if you want to be able to stop processing and not to put
        // auth messages into the inbox.
        ifilter.setPriority(1000);
        ifilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        // Create and hold the receiver. We need to unregister on shutdown
        mActivity.registerReceiver(incomingSms, ifilter );
    }

    public void unregisterMessageListener(){
        mActivity.unregisterReceiver(incomingSms);
    }
}
