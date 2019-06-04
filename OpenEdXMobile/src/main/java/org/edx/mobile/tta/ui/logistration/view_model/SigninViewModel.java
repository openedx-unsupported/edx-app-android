package org.edx.mobile.tta.ui.logistration.view_model;


import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.text.Editable;
import android.text.TextWatcher;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.enums.SurveyType;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.logistration.UserInfoActivity;
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
        mActivity.showLoading();
        mDataManager.login(cellphone.get(), password.get(), new OnResponseCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                mActivity.hideLoading();
                performBackgroundTasks();

                if (data.profile.name == null || data.profile.name.equals("") ||
                        data.profile.name.equals(data.profile.username)) {
                    ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    mDataManager.refreshLocalDatabase();
                    ActivityUtil.gotoPage(mActivity, LandingActivity.class);

                    mActivity.analytic.addMxAnalytics_db(null, Action.SignIn, Nav.signin.name(),
                            Source.Mobile, null);

                }
                mActivity.finish();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                if (e instanceof AuthException){
                    mFragment.showErrorDialog(mActivity.getString(R.string.login_error),
                            mActivity.getString(R.string.login_failed));
                }
            }
        });
        /*new LoginTask(mActivity, cellphone.get(), password.get()){
            @Override
            protected void onSuccess(AuthResponse authResponse) throws Exception {
                super.onSuccess(authResponse);
                mActivity.hideLoading();
                ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                mActivity.finish();
            }

            @Override
            protected void onException(Exception ex) {
//                super.onException(ex);
                mActivity.hideLoading();
                if (ex instanceof AuthException){
                    mFragment.showErrorDialog(mActivity.getString(R.string.login_error),
                            mActivity.getString(R.string.login_failed));
                }
            }
        }.execute();*/
    }

    private void performBackgroundTasks(){
        mDataManager.setCustomFieldAttributes(null);
        mDataManager.setConnectCookies();
        mDataManager.checkSurvey(mActivity, SurveyType.Login);
        mDataManager.updateFirebaseToken();
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
