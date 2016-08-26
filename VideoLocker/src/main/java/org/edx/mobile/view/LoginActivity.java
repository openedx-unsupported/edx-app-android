package org.edx.mobile.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

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
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.dialog.ResetPasswordActivity;
import org.edx.mobile.view.dialog.SimpleAlertDialog;
import org.edx.mobile.view.login.LoginPresenter;

public class LoginActivity extends PresenterActivity<LoginPresenter, LoginPresenter.LoginViewInterface> implements SocialLoginDelegate.MobileLoginCallback {

    private SocialLoginDelegate socialLoginDelegate;
    private ActivityLoginBinding activityLoginBinding;

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
        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this, environment.getConfig(), environment.getLoginPrefs());

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

        // enable login buttons at launch
        tryToSetUIInteraction(true);

        Config config = environment.getConfig();
        setTitle(ResourceUtil.getFormattedString(getResources(), R.string.login_title, "platform_name", config.getPlatformName()));

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tryToSetUIInteraction(true);
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data);
    }

    private void displayLastEmailId() {
        activityLoginBinding.emailEt.setText(loginPrefs.getLastAuthenticatedEmail());
    }

    public void callServerForLogin() {
        if (!NetworkUtil.isConnected(this)) {
            showErrorDialog(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
            return;
        }

        final String emailStr = activityLoginBinding.emailEt.getText().toString().trim();
        final String passwordStr = activityLoginBinding.passwordEt.getText().toString().trim();

        if (activityLoginBinding.emailEt != null && emailStr.length() == 0) {
            showErrorDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_email));
            activityLoginBinding.emailEt.requestFocus();
        } else if (activityLoginBinding.passwordEt != null && passwordStr.length() == 0) {
            showErrorDialog(getString(R.string.login_error),
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

    private static final int RESET_PASSWORD_REQUEST_CODE = 0;

    private void showResetPasswordDialog() {
        startActivityForResult(ResetPasswordActivity.newIntent(getEmail()), RESET_PASSWORD_REQUEST_CODE);
    }

    public void showEulaDialog() {
        environment.getRouter().showWebViewDialog(this, getString(R.string.eula_file_link), getString(R.string.end_user_title));
    }

    public void showNoNetworkDialog() {
        Bundle args = new Bundle();
        args.putString(SimpleAlertDialog.EXTRA_TITLE, getString(R.string.reset_no_network_title));
        args.putString(SimpleAlertDialog.EXTRA_MESSAGE, getString(R.string.reset_no_network_message));

        SimpleAlertDialog noNetworkFragment = SimpleAlertDialog.newInstance(args);
        noNetworkFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        noNetworkFragment.show(getSupportFragmentManager(), "dialog");
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
        setResult(RESULT_OK);
        finish();
    }

    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        tryToSetUIInteraction(true);


        // handle if this is a LoginException
        if (ex != null && ex instanceof LoginException) {
            LoginErrorMessage error = (((LoginException) ex).getLoginErrorMessage());

            showErrorDialog(
                    error.getMessageLine1(),
                    (error.getMessageLine2() != null) ?
                            error.getMessageLine2() : getString(R.string.login_failed));
        } else {
            showErrorDialog(getString(R.string.login_error), getString(R.string.error_unknown));
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
}
