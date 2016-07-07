package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;

import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginTask;
import org.edx.mobile.databinding.ActivityLoginBinding;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.dialog.ResetPasswordDialog;
import org.edx.mobile.view.dialog.SimpleAlertDialog;
import org.edx.mobile.view.dialog.SuccessDialogFragment;
import org.edx.mobile.view.login.LoginPresenter;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends PresenterActivity<LoginPresenter, LoginPresenter.LoginViewInterface> implements SocialLoginDelegate.MobileLoginCallback {

    public String emailStr;
    private SimpleAlertDialog NoNetworkFragment;
    private ResetPasswordDialog resetDialog;
    private SuccessDialogFragment successFragment;
    private SocialLoginDelegate socialLoginDelegate;

    private ActivityLoginBinding activityLoginBinding;

    @Inject
    LoginPrefs loginPrefs;

    public static Intent newIntent(Context context) {
        Intent launchIntent = new Intent(context, LoginActivity.class);
        if (!(context instanceof Activity))
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launchIntent;
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
        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this, environment.getConfig(), environment.getLoginPrefs());

        activityLoginBinding.socialAuth.facebookButton.imgFacebook.setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK));
        activityLoginBinding.socialAuth.googleButton.imgGoogle.setOnClickListener(
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
                    showNoNetworkDialog();
                }
            }
        });

        String platformName = environment.getConfig().getPlatformName();
        CharSequence licenseText = ResourceUtil.getFormattedString(getResources(), R.string.licensing_agreement, "platform_name", platformName);
        activityLoginBinding.endUserAgreementTv.setText(licenseText);
        activityLoginBinding.endUserAgreementTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEulaDialog();
            }
        });

        environment.getSegment().trackScreenView(ISegment.Screens.LOGIN);

        activityLoginBinding.panelCustomActionBar.actionbarCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // enable login buttons at launch
        tryToSetUIInteraction(true);

        Config config = environment.getConfig();

        activityLoginBinding.panelCustomActionBar.activityTitle.setText(
                ResourceUtil.getFormattedString(getResources(), R.string.login_title, "platform_name", config.getPlatformName()));

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
            public void setSocialLoginButtons(boolean googleEnabled, boolean facebookEnabled) {
                if (!facebookEnabled && !googleEnabled) {
                    activityLoginBinding.panelLoginSocial.setVisibility(View.GONE);
                } else if (!facebookEnabled) {
                    activityLoginBinding.socialAuth.facebookButton.facebookLayout.setVisibility(View.GONE);
                } else if (!googleEnabled) {
                    activityLoginBinding.socialAuth.googleButton.googleLayout.setVisibility(View.GONE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tryToSetUIInteraction(true);
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data);
    }


    protected void onResume() {
        super.onResume();
        //MOB-1343 : app enter here when user in the login window and lock the screen
        if (loginPrefs.getCurrentUserProfile() != null) {
            Intent intent = new Intent(LoginActivity.this, MyCoursesListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void displayLastEmailId() {
        activityLoginBinding.emailEt.setText(loginPrefs.getLastAuthenticatedEmail());
    }

    public void callServerForLogin() {

        if (!NetworkUtil.isConnected(this)) {
            showErrorMessage(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
            return;
        }

        emailStr = activityLoginBinding.emailEt.getText().toString().trim();
        String passwordStr = activityLoginBinding.passwordEt.getText().toString().trim();

        if (activityLoginBinding.emailEt != null && emailStr.length() == 0) {
            showErrorMessage(getString(R.string.login_error),
                    getString(R.string.error_enter_email));
            activityLoginBinding.emailEt.requestFocus();
        } else if (activityLoginBinding.passwordEt != null && passwordStr.length() == 0) {
            showErrorMessage(getString(R.string.login_error),
                    getString(R.string.error_enter_password));
            activityLoginBinding.passwordEt.requestFocus();
        } else {
            activityLoginBinding.emailEt.setEnabled(false);
            activityLoginBinding.passwordEt.setEnabled(false);
            activityLoginBinding.forgotPasswordTv.setEnabled(false);
            activityLoginBinding.endUserAgreementTv.setEnabled(false);

            clearDialogs();

            LoginTask logintask = new LoginTask(this, activityLoginBinding.emailEt.getText().toString().trim(),
                    activityLoginBinding.passwordEt.getText().toString()) {
                @Override
                public void onSuccess(@NonNull AuthResponse result) {
                    onUserLoginSuccess(result.profile);
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof AuthException) {
                        onUserLoginFailure(new LoginException(new LoginErrorMessage(
                                getString(R.string.login_error),
                                getString(R.string.login_failed))), null, null);
                    } else {
                        super.onException(ex);
                    }
                    tryToSetUIInteraction(true);
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
        clearDialogs();
        resetDialog = new ResetPasswordDialog() {
            @Override
            protected void onResetSuccessful() {
                super.onResetSuccessful();
                if (isActivityStarted())
                    showResetSuccessDialog();
            }

            @Override
            protected void onResetFailed(ResetPasswordResponse result) {
                super.onResetFailed(result);
                showResetFailure(result.getPrimaryReason());
            }
        };
        Bundle bundle = new Bundle();
        bundle.putString("login_email", getEmail());
        resetDialog.setArguments(bundle);
        resetDialog.show(getSupportFragmentManager(), "show");
    }

    public void showResetSuccessDialog() {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.success_dialog_title_help));
        dialogMap.put("message_1",
                getString(R.string.success_dialog_message_help));

        successFragment = SuccessDialogFragment.newInstance(dialogMap);
        successFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        successFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void showEulaDialog() {
        clearDialogs();
        environment.getRouter().showWebViewDialog(this, getString(R.string.eula_file_link), getString(R.string.end_user_title));
    }

    public void showResetFailure(String text) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.title_reset_password_failed));
        dialogMap.put("message_1", text);

        successFragment = SuccessDialogFragment.newInstance(dialogMap);
        successFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        successFragment.show(getSupportFragmentManager(), "dialog");

    }

    public void showNoNetworkDialog() {
        Bundle args = new Bundle();
        args.putString(SimpleAlertDialog.EXTRA_TITLE, getString(R.string.reset_no_network_title));
        args.putString(SimpleAlertDialog.EXTRA_MESSAGE, getString(R.string.reset_no_network_message));

        NoNetworkFragment = SimpleAlertDialog.newInstance(args);
        NoNetworkFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        NoNetworkFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean showErrorMessage(String header, String message, boolean isPersistent) {
        if (message != null) {
            return super.showErrorMessage(header, message, isPersistent);
        } else {
            return super.showErrorMessage(header, getString(R.string.login_failed), isPersistent);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Animation errorMessageAnim = activityLoginBinding.errorLayout.getAnimation();
        if (errorMessageAnim == null || errorMessageAnim.hasEnded()) {
            ViewAnimationUtil.hideMessageBar(activityLoginBinding.errorLayout);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        hideErrorMessage();
    }

    @Override
    protected void onOffline() {
        super.onOffline();
        showErrorMessage(getString(R.string.no_connectivity),
                getString(R.string.network_not_connected), false);
    }

    private void clearDialogs() {
        if (resetDialog != null) {
            resetDialog.dismiss();
        }
    }

    @Override
    public boolean createOptionsMenu(Menu menu) {
        // Login screen doesn't have any menu
        return true;
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
        if (isActivityStarted()) {
            // do NOT launch next screen if app minimized
            environment.getRouter().showMyCourses(this);
            // but finish this screen anyways as login is succeeded
            finish();
        }
    }

    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        tryToSetUIInteraction(true);


        // handle if this is a LoginException
        if (ex != null && ex instanceof LoginException) {
            LoginErrorMessage error = (((LoginException) ex).getLoginErrorMessage());

            showErrorMessage(
                    error.getMessageLine1(),
                    (error.getMessageLine2() != null) ?
                            error.getMessageLine2() : getString(R.string.login_failed));
        } else {
            showErrorMessage(getString(R.string.login_error), getString(R.string.error_unknown));
            logger.error(ex);
        }
    }

    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        if (enable) {
            unblockTouch();
            activityLoginBinding.loginButtonLayout.setBackgroundResource(R.drawable.bt_signin_active);
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.login));
        } else {
            blockTouch();
            activityLoginBinding.loginButtonLayout.setBackgroundResource(R.drawable.new_bt_signin_active);
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.signing_in));
        }


        activityLoginBinding.socialAuth.facebookButton.imgFacebook.setClickable(enable);
        activityLoginBinding.socialAuth.googleButton.imgGoogle.setClickable(enable);

        activityLoginBinding.emailEt.setEnabled(enable);
        activityLoginBinding.passwordEt.setEnabled(enable);

        activityLoginBinding.forgotPasswordTv.setEnabled(enable);
        activityLoginBinding.endUserAgreementTv.setEnabled(enable);

        return true;
    }
}
