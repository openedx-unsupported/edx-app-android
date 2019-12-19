package org.humana.mobile.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.inject.Inject;

import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.authentication.AuthResponse;
import org.humana.mobile.authentication.LoginTask;
import org.humana.mobile.databinding.ActivityLoginBinding;
import org.humana.mobile.exception.LoginErrorMessage;
import org.humana.mobile.exception.LoginException;
import org.humana.mobile.http.HttpStatus;
import org.humana.mobile.http.HttpStatusException;
import org.humana.mobile.model.api.ProfileModel;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.social.SocialFactory;
import org.humana.mobile.social.SocialLoginDelegate;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.constants.Constants;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.firebase.FirebaseHelper;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.landing.LandingActivity;
import org.humana.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.humana.mobile.tta.ui.programs.selectprogram.SelectProgramActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.AppStoreUtils;
import org.humana.mobile.util.Config;
import org.humana.mobile.util.IntentFactory;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.util.TextUtils;
import org.humana.mobile.util.images.ErrorUtils;
import org.humana.mobile.view.dialog.ResetPasswordDialogFragment;
import org.humana.mobile.view.login.LoginPresenter;

import java.util.List;

import static org.humana.mobile.util.BrowserUtil.loginPrefs;

public class LoginActivity
        extends PresenterActivity<LoginPresenter, LoginPresenter.LoginViewInterface>
        implements SocialLoginDelegate.MobileLoginCallback {
    private SocialLoginDelegate socialLoginDelegate;
    private ActivityLoginBinding activityLoginBinding;
    private DataManager mDataManager;

    @Inject
    LoginPrefs loginPrefs;

    @NonNull
    public static Intent newIntent() {
        return IntentFactory.newIntentForComponent(LoginActivity.class);
    }

    @NonNull
    @Override
    protected LoginPresenter createPresenter(@Nullable Bundle savedInstanceState) {
        return new LoginPresenter(
                environment.getConfig(),
                new NetworkUtil.ZeroRatedNetworkInfo(getApplicationContext(), environment.getConfig()));
    }

    @NonNull
    @Override
    protected LoginPresenter.LoginViewInterface createView(@Nullable Bundle savedInstanceState) {
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        hideSoftKeypad();
        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this,
                environment.getConfig(), environment.getLoginPrefs(), SocialLoginDelegate.Feature.SIGN_IN);

        activityLoginBinding.socialAuth.facebookButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK));
        activityLoginBinding.socialAuth.googleButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE));

        activityLoginBinding.loginButtonLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for Validation˜
                callServerForLogin();
            }
        });

        activityLoginBinding.forgotPasswordTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling help dialog
                if (NetworkUtil.isConnected(LoginActivity.this)) {
                    showResetPasswordDialog();
                } else {
                    showAlertDialog(getString(R.string.reset_no_network_title), getString(R.string.network_not_connected));
                }
            }
        });

        mDataManager = DataManager.getInstance(this);
        activityLoginBinding.endUserAgreementTv.setMovementMethod(LinkMovementMethod.getInstance());
        activityLoginBinding.endUserAgreementTv.setText(TextUtils.generateLicenseText(getResources(), R.string.by_signing_in));

        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LOGIN);

        // enable login buttons at launch
        tryToSetUIInteraction(true);

        Config config = environment.getConfig();
        setTitle(getString(R.string.login_title));

        String envDisplayName = config.getEnvironmentDisplayName();
        if (envDisplayName != null && envDisplayName.length() > 0) {
            activityLoginBinding.versionEnvTv.setVisibility(View.VISIBLE);
            String versionName = BuildConfig.VERSION_NAME;
            String text = String.format("%s %s %s",
                    getString(R.string.label_version), versionName, envDisplayName);
            activityLoginBinding.versionEnvTv.setText(text);
        }

        return new LoginPresenter.LoginViewInterface() {
            @Override
            public void disableToolbarNavigation() {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setHomeButtonEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowHomeEnabled(false);
                }
            }

            @Override
            public void setSocialLoginButtons(boolean googleEnabled, boolean facebookEnabled) {
                if (!facebookEnabled && !googleEnabled) {
                    activityLoginBinding.panelLoginSocial.setVisibility(View.GONE);
                } else if (!facebookEnabled) {
                    activityLoginBinding.socialAuth.facebookButton.getRoot().setVisibility(View.GONE);
                } else if (!googleEnabled) {
                    activityLoginBinding.socialAuth.googleButton.getRoot().setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socialLoginDelegate.onActivityDestroyed();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", activityLoginBinding.emailEt.getText().toString().trim());

        socialLoginDelegate.onActivitySaveInstanceState(outState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (activityLoginBinding.emailEt.getText().toString().length() == 0) {
            displayLastEmailId();
        }

        socialLoginDelegate.onActivityStarted();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            activityLoginBinding.emailEt.setText(savedInstanceState.getString("username"));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tryToSetUIInteraction(true);
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ResetPasswordDialogFragment.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    showAlertDialog(getString(R.string.success_dialog_title_help),
                            getString(R.string.success_dialog_message_help));
                }
                break;
            }
        }
    }

    private void displayLastEmailId() {
        activityLoginBinding.emailEt.setText(loginPrefs.getLastAuthenticatedEmail());
    }

    public void callServerForLogin() {
        if (!NetworkUtil.isConnected(this)) {
            showAlertDialog(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
            return;
        }

        final String emailStr = activityLoginBinding.emailEt.getText().toString().trim();
        final String passwordStr = activityLoginBinding.passwordEt.getText().toString().trim();

        if (activityLoginBinding.emailEt != null && emailStr.length() == 0) {
            showAlertDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_email));
            activityLoginBinding.emailEt.requestFocus();
        } else if (activityLoginBinding.passwordEt != null && passwordStr.length() == 0) {
            showAlertDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_password));
            activityLoginBinding.passwordEt.requestFocus();
        } else {
            activityLoginBinding.emailEt.setEnabled(false);
            activityLoginBinding.passwordEt.setEnabled(false);
            activityLoginBinding.forgotPasswordTv.setEnabled(false);
            activityLoginBinding.endUserAgreementTv.setEnabled(false);

            LoginTask logintask = new LoginTask(this, activityLoginBinding.emailEt.getText().toString().trim(),
                    activityLoginBinding.passwordEt.getText().toString()) {
                @Override
                public void onSuccess(@NonNull AuthResponse result) {
                    onUserLoginSuccess(result.profile);
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        onUserLoginFailure(new LoginException(new LoginErrorMessage(
                                getString(R.string.login_error),
                                getString(R.string.login_failed))), null, null);
                    } else {
                        onUserLoginFailure(ex, null, null);
                    }
                }
            };
            tryToSetUIInteraction(false);
            logintask.setProgressDialog(activityLoginBinding.progress.progressIndicator);
            logintask.execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        socialLoginDelegate.onActivityStopped();
    }

    public String getEmail() {
        return activityLoginBinding.emailEt.getText().toString().trim();
    }

    private void showResetPasswordDialog() {
        ResetPasswordDialogFragment.newInstance(getEmail()).show(getSupportFragmentManager(), null);
    }

    // make sure that on the login activity, all errors showLoading up as a dialog as opposed to a flying snackbar
    @Override
    public void showAlertDialog(@Nullable String header, @NonNull String message) {
        super.showAlertDialog(header, message);
    }


    /**
     * Starts fetching profile of the user after login by Facebook or Google.
     *
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend, Task task) {
        tryToSetUIInteraction(false);
        task.setProgressDialog(activityLoginBinding.progress.progressIndicator);
    }

    public void onUserLoginSuccess(ProfileModel profile) {
        setResult(RESULT_OK);
        getFirebaseToken();
        /*if (!environment.getConfig().isRegistrationEnabled()) {
            environment.getRouter().showMainDashboard(this);
        }*/
        if (environment.getUserPrefs().getProfile() != null) {
            //environment.getRouter().showMainDashboard(SplashActivity.this);
            activityLoginBinding.progress.progressIndicator.setVisibility(View.VISIBLE);
            activityLoginBinding.progress.getRoot().setVisibility(View.VISIBLE);
            tryToSetUIInteraction(false);
            mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
                @Override
                public void onSuccess(List<Program> data) {
                    if (data.size() == 0){
                        ActivityUtil.gotoPage(LoginActivity.this, LandingActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    }
                    else if (data.size() == 1) {
                        mDataManager.getLoginPrefs().setProgramTitle(data.get(0).getTitle());
                        mDataManager.getLoginPrefs().setProgramId(data.get(0).getId());
                        mDataManager.getLoginPrefs().setParentId(data.get(0).getParent_id());

                        Constants.isSinglePrg = true;
                        getSection();
                    } else {
                        activityLoginBinding.progress.progressIndicator.setVisibility(View.GONE);
                        tryToSetUIInteraction(true);
                        finish();
                        ActivityUtil.gotoPage(LoginActivity.this, SelectProgramActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }

                }

                @Override
                public void onFailure(Exception e) {
                    activityLoginBinding.progress.progressIndicator.setVisibility(View.GONE);
                    tryToSetUIInteraction(true);
                    ActivityUtil.gotoPage(LoginActivity.this, LandingActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
            });
        } else {
            finish();
        }

//        ActivityUtil.gotoPage(LoginActivity.this, SelectProgramActivity.class,
//                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private void getSection() {
        mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(), new OnResponseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {
                activityLoginBinding.progress.progressIndicator.setVisibility(View.GONE);
                tryToSetUIInteraction(true);
                if (data.size() == 1) {
                    mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                    mDataManager.getLoginPrefs().setRole(data.get(0).getRole());


                    ActivityUtil.gotoPage(LoginActivity.this, LandingActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Constants.isSingleRow = true;
                    finish();
                } else {
                    ActivityUtil.gotoPage(LoginActivity.this, SelectSectionActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
//                for (Section unit: data){
//                    if (!selectedSections.contains(unit)){
//                        selectedSections.add(unit);
//                    }
//                }
            }

            @Override
            public void onFailure(Exception e) {
                activityLoginBinding.progress.progressIndicator.setVisibility(View.GONE);
                tryToSetUIInteraction(true);
            }
        });
    }

    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        tryToSetUIInteraction(true);

        if (ex != null && ex instanceof LoginException) {
            LoginErrorMessage errorMessage = (((LoginException) ex).getLoginErrorMessage());
            showAlertDialog(
                    errorMessage.getMessageLine1(),
                    (errorMessage.getMessageLine2() != null) ?
                            errorMessage.getMessageLine2() : getString(R.string.login_failed));
        } else if (ex != null && ex instanceof HttpStatusException &&
                ((HttpStatusException) ex).getStatusCode() == HttpStatus.UPGRADE_REQUIRED) {
            LoginActivity.this.showAlertDialog(null,
                    getString(R.string.app_version_unsupported_login_msg),
                    getString(R.string.label_update),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppStoreUtils.openAppInAppStore(LoginActivity.this);
                        }
                    }, getString(android.R.string.cancel), null);
        } else {
            showAlertDialog(getString(R.string.login_error), ErrorUtils.getErrorMessage(ex, LoginActivity.this));
            logger.error(ex);
        }
    }

    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        if (enable) {
            unblockTouch();
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.login));
        } else {
            blockTouch();
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.signing_in));
        }


        activityLoginBinding.socialAuth.facebookButton.getRoot().setClickable(enable);
        activityLoginBinding.socialAuth.googleButton.getRoot().setClickable(enable);

        activityLoginBinding.emailEt.setEnabled(enable);
        activityLoginBinding.passwordEt.setEnabled(enable);

        activityLoginBinding.forgotPasswordTv.setEnabled(enable);
        activityLoginBinding.endUserAgreementTv.setEnabled(enable);

        return true;
    }
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        //update Firebase token , we will update it on sign-in or registration too

        Log.d("firebaseToken",token);
        if(loginPrefs==null || loginPrefs.getUsername()==null || loginPrefs.getUsername().equals("") ||this.getApplicationContext()==null)
            return;

        FirebaseHelper fireBaseHelper=new FirebaseHelper();
        try
        {
            fireBaseHelper.updateFirebasetokenToServer(this.getApplicationContext(),
                    fireBaseHelper.getFireBaseParams(loginPrefs.getUsername()));
        }
        catch (Exception ex)
        {
            Log.d("ManpraxFirebase","MyFirebaseInstanceIDService class ID update crash");
        }
    }

    private void getFirebaseToken(){
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token!=null) {
            sendRegistrationToServer(token);
        }
    }
}
