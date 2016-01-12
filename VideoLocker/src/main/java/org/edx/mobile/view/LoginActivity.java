package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.LoginTask;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.dialog.ResetPasswordDialog;
import org.edx.mobile.view.dialog.SimpleAlertDialog;
import org.edx.mobile.view.dialog.SuccessDialogFragment;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseFragmentActivity implements SocialLoginDelegate.MobileLoginCallback {

    private TextView login_tv;
    private EditText email_et, password_et;

    private SimpleAlertDialog NoNetworkFragment;

    private ResetPasswordDialog resetDialog;
    private SuccessDialogFragment successFragment;
    private ProgressBar progressbar;
    private ViewGroup loginButtonLayout;
    public String emailStr;
    private TextView forgotPassword_tv;
    private TextView eulaTv;
    private LinearLayout errorLayout;
    private TextView errorHeader;
    private TextView errorMessage;
    private SocialLoginDelegate socialLoginDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();

        // setup for social login
        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this, environment.getConfig());

        ImageView imgFacebook = (ImageView) findViewById(R.id.img_facebook);
        ImageView imgGoogle = (ImageView) findViewById(R.id.img_google);
        imgFacebook.setOnClickListener(socialLoginDelegate.createSocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK));
        imgGoogle.setOnClickListener(socialLoginDelegate.createSocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE));


        email_et = (EditText) findViewById(R.id.email_et);

        password_et = (EditText) findViewById(R.id.password_et);
        progressbar = (ProgressBar) findViewById(R.id.progress_indicator);
        login_tv = (TextView) findViewById(R.id.login_btn_tv);

        loginButtonLayout = (ViewGroup) findViewById(R.id.login_button_layout);
        loginButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Check for Validation
                callServerForLogin();
            }
        });

        forgotPassword_tv = (TextView) findViewById(R.id.forgot_password_tv);
        forgotPassword_tv.setOnClickListener(new OnClickListener() {

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

        eulaTv = (TextView) findViewById(R.id.end_user_agreement_tv);
        String platformName = environment.getConfig().getPlatformName();
        CharSequence licenseText = ResourceUtil.getFormattedString(getResources(), R.string.licensing_agreement, "platform_name", platformName);
        eulaTv.setText(licenseText);
        eulaTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEulaDialog();
            }
        });

        errorLayout = (LinearLayout) findViewById(R.id.error_layout);
        errorHeader = (TextView) findViewById(R.id.error_header);
        errorMessage = (TextView) findViewById(R.id.error_message);

        environment.getSegment().trackScreenView(ISegment.Screens.LOGIN);

        View closeButton = findViewById(R.id.actionbar_close_btn);
        if (closeButton != null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // enable login buttons at launch
        tryToSetUIInteraction(true);

        Config config = environment.getConfig();
        // check if third party traffic is enabled
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(getApplicationContext(), config);

        if (isOnZeroRatedNetwork) {
            findViewById(R.id.panel_login_social).setVisibility(View.GONE);
        } else {
            if (!config.getFacebookConfig().isEnabled()
                    && !config.getGoogleConfig().isEnabled()) {
                findViewById(R.id.panel_login_social).setVisibility(View.GONE);
            } else if (!config.getFacebookConfig().isEnabled()) {
                findViewById(R.id.facebook_layout).setVisibility(View.GONE);
            } else if (!config.getGoogleConfig().isEnabled()) {
                findViewById(R.id.google_layout).setVisibility(View.GONE);
            }
        }

        TextView customTitle = (TextView) findViewById(R.id.activity_title);
        if (customTitle != null) {
            customTitle.setText(ResourceUtil.getFormattedString(getResources(), R.string.login_title, "platform_name", config.getPlatformName()));
        }

        TextView version_tv = (TextView) findViewById(R.id.tv_version_no);
        try {
            String envDisplayName = config.getEnvironmentDisplayName();

            if (envDisplayName != null && envDisplayName.length() > 0) {
                version_tv.setVisibility(View.VISIBLE);
                String versionName = PropertyUtil.getManifestVersionName(this);
                String text = String.format("%s %s %s",
                        getString(R.string.label_version), versionName, envDisplayName);
                version_tv.setText(text);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socialLoginDelegate.onActivityDestroyed();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", email_et.getText().toString().trim());

        socialLoginDelegate.onActivitySaveInstanceState(outState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (email_et.getText().toString().length() == 0) {
            displayLastEmailId();
        }

        socialLoginDelegate.onActivityStarted();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            email_et.setText(savedInstanceState.getString("username"));
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
        PrefManager pm = new PrefManager(LoginActivity.this, PrefManager.Pref.LOGIN);
        //MOB-1343 : app enter here when user in the login window and lock the screen
        if (pm.getCurrentUserProfile() != null) {
            Intent intent = new Intent(LoginActivity.this, MyCoursesListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void displayLastEmailId() {
        PrefManager pref = new PrefManager(this, PrefManager.Pref.LOGIN);
        String emailId = pref.getString("email");
        email_et.setText(emailId);
    }

    public ProgressBar getProgressBar() {
        return this.progressbar;
    }

    public void callServerForLogin() {

        if (NetworkUtil.isConnected(this)) {
            emailStr = email_et.getText().toString().trim();
            String passwordStr = password_et.getText().toString().trim();

            if (email_et != null && emailStr.length() == 0) {
                showErrorMessage(getString(R.string.login_error),
                        getString(R.string.error_enter_email));
                email_et.requestFocus();
            } else if (password_et != null && passwordStr.length() == 0) {
                showErrorMessage(getString(R.string.login_error),
                        getString(R.string.error_enter_password));
                password_et.requestFocus();
            } else {
                email_et.setEnabled(false);
                password_et.setEnabled(false);
                forgotPassword_tv.setEnabled(false);
                eulaTv.setEnabled(false);

                clearDialogs();

                LoginTask logintask = new LoginTask(this, email_et.getText().toString().trim(),
                        password_et.getText().toString()) {
                    @Override
                    public void onSuccess(AuthResponse result) {
                        try {
                            if (result != null && result.hasValidProfile()) {
                                onUserLoginSuccess(result.profile);
                            } else {
                                LoginErrorMessage errorMsg =
                                        new LoginErrorMessage(
                                                getString(R.string.login_error),
                                                getString(R.string.login_failed));
                                throw new LoginException(errorMsg);
                            }
                        } catch (LoginException ex) {
                            logger.error(ex);
                            handle(ex);
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        onUserLoginFailure(ex, null, null);
                    }

                };

                tryToSetUIInteraction(false);
                logintask.setProgressDialog(progressbar);
                logintask.execute();
            }
        } else {
            showErrorMessage(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        socialLoginDelegate.onActivityStopped();
    }

    public String getEmail() {
        String email = email_et.getText().toString().trim();

        return email;
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
        showWebDialog(getString(R.string.eula_file_link),
                getString(R.string.end_user_title));
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
        Animation errorMessageAnim = errorLayout.getAnimation();
        if (errorMessageAnim == null || errorMessageAnim.hasEnded()) {
            ViewAnimationUtil.hideMessageBar(errorLayout);
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

    private void myCourseScreen() {
        if (isActivityStarted()) {
            // do NOT launch next screen if app minimized
            environment.getRouter().showMyCourses(this);
            // but finish this screen anyways as login is succeeded
            finish();
        }
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
        task.setProgressDialog(progressbar);
    }

    public void onUserLoginSuccess(ProfileModel profile) throws LoginException {

        // save this email id
        PrefManager pref = new PrefManager(this, PrefManager.Pref.LOGIN);
        pref.put("email", email_et.getText().toString().trim());

        pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, "none");

        environment.getSegment().identifyUser(profile.id.toString(), profile.email,
                email_et.getText().toString().trim());

        String backendKey = pref.getString(PrefManager.Key.SEGMENT_KEY_BACKEND);
        if (backendKey != null) {
            environment.getSegment().trackUserLogin(backendKey);
        }

        //segIO.trackDeviceDetails();
        environment.getNotificationDelegate().resubscribeAll();

        myCourseScreen();
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
            logger.error(ex);
        }
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }


    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        if (enable) {
            unblockTouch();
            loginButtonLayout.setBackgroundResource(R.drawable.bt_signin_active);
            loginButtonLayout.setEnabled(enable);
            login_tv.setText(getString(R.string.login));
        } else {
            blockTouch();
            loginButtonLayout.setBackgroundResource(R.drawable.new_bt_signin_active);
            loginButtonLayout.setEnabled(enable);
            login_tv.setText(getString(R.string.signing_in));
        }


        ImageView imgFacebook = (ImageView) findViewById(R.id.img_facebook);
        ImageView imgGoogle = (ImageView) findViewById(R.id.img_google);
        imgFacebook.setClickable(enable);
        imgGoogle.setClickable(enable);

        email_et.setEnabled(enable);
        password_et.setEnabled(enable);

        forgotPassword_tv.setEnabled(enable);
        eulaTv.setEnabled(enable);

        return true;
    }

}
