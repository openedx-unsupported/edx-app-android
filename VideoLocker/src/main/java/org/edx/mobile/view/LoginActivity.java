package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SocialLoginResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.ISocial;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.task.LoginTask;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.dialog.ResetPasswordDialog;
import org.edx.mobile.view.dialog.SimpleAlertDialog;
import org.edx.mobile.view.dialog.SuccessDialogFragment;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseFragmentActivity {

    private TextView login_tv;
    private EditText email_et, password_et;

    private SimpleAlertDialog NoNetworkFragment;

    private ResetPasswordDialog resetDialog;
    private SuccessDialogFragment successFragment;
    private ProgressBar progressbar;
    private RelativeLayout loginButtonLayout;
    public String emailStr;
    private TextView forgotPassword_tv;
    private TextView eulaTv;
    private ISocial google, facebook;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();

        runOnTick = false;

        // setup for social login
        SocialLogin();

        google.onActivityCreated(this, savedInstanceState);
        facebook.onActivityCreated(this, savedInstanceState);

        email_et = (EditText) findViewById(R.id.email_et);

        password_et = (EditText) findViewById(R.id.password_et);
        progressbar = (ProgressBar) findViewById(R.id.login_spinner);
        login_tv = (TextView) findViewById(R.id.login_btn_tv);

        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
        }

        loginButtonLayout = (RelativeLayout) findViewById(R.id.login_button_layout);
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
                if (!AppConstants.offline_flag) {
                    showResetPasswordDialog();
                } else {
                    showNoNetworkDialog();
                }
            }
        });

        eulaTv = (TextView) findViewById(R.id.end_user_agreement_tv);
        eulaTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEulaDialog();
            }
        });

        try{
            segIO.screenViewsTracking("Login");
        }catch(Exception e){
            logger.error(e);
        }

        // enable login buttons at launch
        setLoginBtnEnabled();

        RelativeLayout closeButtonLayout = (RelativeLayout)
                findViewById(R.id.actionbar_close_btn_layout);
        if(closeButtonLayout!=null){
            closeButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // enable login buttons at launch
        setLoginBtnEnabled();

        // check if third party traffic is enabled
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(getApplicationContext());

        if (isOnZeroRatedNetwork) {
            findViewById(R.id.panel_login_social).setVisibility(View.GONE);
        } else {
            if (!Config.getInstance().getFacebookConfig().isEnabled()
                    && !Config.getInstance().getGoogleConfig().isEnabled()) {
                findViewById(R.id.panel_login_social).setVisibility(View.GONE);
            }
            else if (!Config.getInstance().getFacebookConfig().isEnabled()) {
                findViewById(R.id.facebook_layout).setVisibility(View.GONE);
            }
            else if (!Config.getInstance().getGoogleConfig().isEnabled()) {
                findViewById(R.id.google_layout).setVisibility(View.GONE);
            }
        }

        TextView customTitle = (TextView) findViewById(R.id.activity_title);
        if(customTitle!=null){
            customTitle.setText(getString(R.string.login_title));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        google.onActivityDestroyed(this);
        facebook.onActivityDestroyed(this);
    }

    private void SocialLogin() {
        google = SocialFactory.getInstance(this, SocialFactory.TYPE_GOOGLE);
        google.setCallback(googleCallback);
        
        facebook = SocialFactory.getInstance(this, SocialFactory.TYPE_FACEBOOK);
        facebook.setCallback(facebookCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", email_et.getText().toString().trim());
        
        google.onActivitySaveInstanceState(this, outState);
        facebook.onActivitySaveInstanceState(this, outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(email_et.getText().toString().length()==0){
            displayLastEmailId();
        }
        
        google.onActivityStarted(this);
        facebook.onActivityStarted(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null){
            email_et.setText(savedInstanceState.getString("username"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        google.onActivityResult(requestCode, resultCode, data);
        facebook.onActivityResult(requestCode, resultCode, data);
    }

    private void displayLastEmailId() {
        PrefManager pref = new PrefManager(this, PrefManager.Pref.LOGIN);
        String emailId = pref.getString("email");
        email_et.setText(emailId);
    }

    public void callServerForLogin() {

        if (!AppConstants.offline_flag) {
            emailStr = email_et.getText().toString().trim();
            String passwordStr = password_et.getText().toString().trim();

            if (email_et != null && emailStr.length() == 0) {
                showErrorMessage(getString(R.string.login_error),
                        getString(R.string.error_enter_email));
                email_et.requestFocus();
            }

            else if (password_et != null && passwordStr.length() == 0) {
                showErrorMessage(getString(R.string.login_error),
                        getString(R.string.error_enter_password));
                password_et.requestFocus();
            }

            else {
                email_et.setEnabled(false);
                password_et.setEnabled(false);
                forgotPassword_tv.setEnabled(false);
                eulaTv.setEnabled(false);

                clearDialogs();

                LoginTask logintask = new LoginTask(this) {
                    @Override
                    public void onFinish(AuthResponse result) {
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
                        } catch(LoginException ex) {
                            logger.error(ex);
                            handle(ex);
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        onUserLoginFailure(ex);
                    }

                };

                setLoginBtnDisabled();
                logintask.setProgressDialog(progressbar);
                logintask.execute(email_et.getText().toString().trim(),
                        password_et.getText().toString());
            }
        } else {
            showErrorMessage(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        google.onActivityStopped(this);
        facebook.onActivityStopped(this);
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
                if(isActivityStarted())
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
        showWebDialog(getString(R.string.eula_file_link), true,
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

    private void setLoginBtnDisabled() {
        blockTouch();
        
        loginButtonLayout.setBackgroundResource(R.drawable.new_bt_signin_active);
        loginButtonLayout.setEnabled(false);
        login_tv.setText(getString(R.string.signing_in));
        
        ImageView imgFacebook=(ImageView)findViewById(R.id.img_facebook);
        ImageView imgGoogle=(ImageView)findViewById(R.id.img_google);
        imgFacebook.setClickable(false);
        imgGoogle.setClickable(false);
    }

    private void setLoginBtnEnabled() {
        unblockTouch();
        
        loginButtonLayout.setBackgroundResource(R.drawable.bt_signin_active);
        loginButtonLayout.setEnabled(true);
        login_tv.setText(getString(R.string.login));
        
        ImageView imgFacebook=(ImageView)findViewById(R.id.img_facebook);
        ImageView imgGoogle=(ImageView)findViewById(R.id.img_google);
        imgFacebook.setClickable(true);
        imgGoogle.setClickable(true);
        imgFacebook.setOnClickListener(facebookClickListener);
        imgGoogle.setOnClickListener(googleClickListener);
    }

    private void showErrorMessage(String header, String message) {
        LinearLayout error_layout = (LinearLayout) findViewById(R.id.error_layout);
        TextView errorHeader = (TextView) findViewById(R.id.error_header);
        TextView errorMessage = (TextView) findViewById(R.id.error_message);
        errorHeader.setText(header);
        if (message != null) {
            errorMessage.setText(message);
        } else {
            errorMessage.setText(getString(R.string.login_failed));
        }
        UiUtil.animateLayouts(error_layout);
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        showErrorMessage(getString(R.string.no_connectivity),
                getString(R.string.network_not_connected));
    }

    private void myCourseScreen() {
        if (isActivityStarted()) {
            // do NOT launch next screen if app minimized
            Router.getInstance().showMyCourses(this);
        }
        
        // but finish this screen anyways as login is succeeded
        finish();
    }

    private void clearDialogs(){
        if(resetDialog!=null){
            resetDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Login screen doesn't have any menu
        return true;
    }

    private ISocial.Callback googleCallback = new ISocial.Callback() {
        
        @Override
        public void onLogin(String accessToken) {
            logger.debug("Google logged in; token= " + accessToken);
            startSocialLogin(accessToken, PrefManager.Value.BACKEND_GOOGLE);
        }

    };
    
    private ISocial.Callback facebookCallback = new ISocial.Callback() {
        
        @Override
        public void onLogin(String accessToken) {
            logger.debug("Facebook logged in; token= " + accessToken);
            startSocialLogin(accessToken, PrefManager.Value.BACKEND_FACEBOOK);
        }
    };

    /**
     * Starts fetching profile of the user after login by Facebook or Google.
     * @param accessToken
     * @param backend
     */
    private void startSocialLogin(String accessToken, String backend) {
        PrefManager pref = new PrefManager(LoginActivity.this, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, accessToken);
        pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, backend);

        setLoginBtnDisabled();
        Task<?> task = new ProfileTask(LoginActivity.this);
        task.setProgressDialog(progressbar);
        task.execute(accessToken, backend);
    }
    
    private void onUserLoginSuccess(ProfileModel profile) throws LoginException {
        if (profile.email == null) {
            // handle this error, show error message
            LoginErrorMessage errorMsg =
                    new LoginErrorMessage(
                            getString(R.string.login_error),
                            getString(R.string.login_failed));
            throw new LoginException(errorMsg);
        }

        // save this email id
        PrefManager pref = new PrefManager(this, PrefManager.Pref.LOGIN);
        pref.put("email", email_et.getText().toString().trim());

        pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, "none");

        segIO.identifyUser(profile.id.toString(), profile.email , 
                email_et.getText().toString().trim());
        
        String backendKey = pref.getString(PrefManager.Key.SEGMENT_KEY_BACKEND);
        if(backendKey!=null){
            segIO.trackUserLogin(backendKey);
        }

        //segIO.trackDeviceDetails();

        myCourseScreen();
    }
    
    private void onUserLoginFailure(Exception ex) {
        setLoginBtnEnabled();
        email_et.setEnabled(true);
        password_et.setEnabled(true);
        forgotPassword_tv.setEnabled(true);
        eulaTv.setEnabled(true);

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
    
    private class ProfileTask extends Task<ProfileModel> {

        public ProfileTask(Context context) {
            super(context);
        }

        @Override
        public void onFinish(ProfileModel result) {
            if (result != null) {
                try {
                    onUserLoginSuccess(result);
                } catch (LoginException ex) {
                    logger.error(ex);
                    handle(ex);
                }
            } 
        }

        @Override
        public void onException(Exception ex) {
            onUserLoginFailure(ex);
        }

        @Override
        protected ProfileModel doInBackground(Object... params) {
            try {
                String accessToken = (String) params[0];
                String backend = (String) params[1];
                
                Api api = new Api(context);
                
                // do SOCIAL LOGIN first
                SocialLoginResponse social = null;
                if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_FACEBOOK)) {
                    social = api.loginByFacebook(accessToken);

                    if (social.isAccountNotLinked()) {
                        throw new LoginException(new LoginErrorMessage(
                                context.getString(R.string.error_account_not_linked_title_fb),
                                context.getString(R.string.error_account_not_linked_desc_fb)));
                    }
                } else if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_GOOGLE)) {
                    social = api.loginByGoogle(accessToken);

                    if (social.isAccountNotLinked()) {
                        throw new LoginException(new LoginErrorMessage(
                                getString(R.string.error_account_not_linked_title_google),
                                getString(R.string.error_account_not_linked_desc_google)));
                    }
                }

                if (social.isSuccess()) {
                    // we got a valid accessToken so profile can be fetched
                    ProfileModel profile = api.getProfile();
                    if (profile.email != null) {
                        // we got valid profile information
                        return profile;
                    }
                }
                throw new LoginException(new LoginErrorMessage(
                        getString(R.string.login_error),
                        getString(R.string.login_failed)));
            } catch (Exception e) {
                logger.error(e);
                handle(e);
            }
            return null;
        }
        
    }
    
    android.view.View.OnClickListener facebookClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (AppConstants.offline_flag) {
                showErrorMessage(getString(R.string.no_connectivity),
                        getString(R.string.network_not_connected));
            } else {
                Task<Void> logout = new Task<Void>(LoginActivity.this) {
                    
                    @Override
                    protected Void doInBackground(Object... arg0) {
                        try {
                            facebook.logout();
                        } catch(Exception ex) {
                            // no need to handle this error
                            logger.error(ex);
                        }
                        return null;
                    }
                    
                    @Override
                    public void onFinish(Void result) {
                        facebook.login();
                    }
                    
                    @Override
                    public void onException(Exception ex) {
                    }
                };
                logout.execute();
            }
        }
    };
    android.view.View.OnClickListener googleClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (AppConstants.offline_flag) {
                showErrorMessage(getString(R.string.no_connectivity),
                        getString(R.string.network_not_connected));
            } else {
                Task<Void> logout = new Task<Void>(LoginActivity.this) {
                    
                    @Override
                    protected Void doInBackground(Object... arg0) {
                        try {
                            google.logout();
                        } catch(Exception ex) {
                            // no need to handle this error
                            logger.error(ex);
                        }
                        return null;
                    }
                    
                    @Override
                    public void onFinish(Void result) {
                        google.login();
                    }
                    
                    @Override
                    public void onException(Exception ex) {
                    }
                };
                logout.execute();
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }
}
