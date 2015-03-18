package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.RegisterResponseFieldError;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.registration.model.RegistrationAgreement;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;
import org.edx.mobile.module.registration.view.IRegistrationFieldView;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.RegisterTask;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.custom.ETitleRowView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegisterActivity extends BaseFragmentActivity
        implements SocialLoginDelegate.MobileLoginCallback {

    private RelativeLayout createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private LinearLayout agreementLayout;
    private ETextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();
    private SocialLoginDelegate socialLoginDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        //The onTick method need not be run in the RegisterActivity
        runOnTick = false;

        try{
            segIO.screenViewsTracking(ISegment.Values.LAUNCH_ACTIVITY);
        }catch(Exception e){
            logger.error(e);
        }

        // setup for social login
        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this);

        ImageView imgFacebook=(ImageView)findViewById(R.id.img_facebook);
        ImageView imgGoogle=(ImageView)findViewById(R.id.img_google);
        imgFacebook.setClickable(true);
        imgGoogle.setClickable(true);
        imgFacebook.setOnClickListener(facebookClickListener);
        imgGoogle.setOnClickListener(googleClickListener);


        createAccountBtn = (RelativeLayout) findViewById(R.id.createAccount_button_layout);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        createAccountTv = (ETextView) findViewById(R.id.create_account_tv);
        requiredFieldsLayout = (LinearLayout) findViewById(R.id.required_fields_layout);
        optionalFieldsLayout = (LinearLayout) findViewById(R.id.optional_fields_layout);
        agreementLayout = (LinearLayout) findViewById(R.id.layout_agreement);
        final ETitleRowView optional_text=(ETitleRowView)findViewById(R.id.optional_field_tv);
        optional_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optionalFieldsLayout.getVisibility() == View.VISIBLE) {
                    optionalFieldsLayout.setVisibility(v.GONE);
                    optional_text.setTitle(getString(R.string.show_optional_text));
                }
                else{
                    optionalFieldsLayout.setVisibility(v.VISIBLE);
                    optional_text.setTitle(getString(R.string.hide_optional_text));
                }
            }
        });

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

        ETextView customTitle = (ETextView) findViewById(R.id.activity_title);
        if(customTitle!=null){
            customTitle.setText(getString(R.string.register_title));
        }

        AppConstants.offline_flag = !NetworkUtil.isConnected(this);

        setupRegistrationForm();
        hideSoftKeypad();
    }

    public void showAgreement(RegistrationAgreement agreement) {
        boolean isInAppEULALink = false;
        try {
            Uri uri = Uri.parse(agreement.getLink());
            if (uri.getScheme().equals("edxapp")
                    && uri.getHost().equals("show_eula")) {
                isInAppEULALink = true;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

        if (isInAppEULALink) {
            // show EULA license that is shipped with app
            showWebDialog(getString(R.string.eula_file_link),
                    true,
                    getString(R.string.end_user_title));
        }
        else {
            // for any other link, open agreement link in a webview container
            showWebDialog(agreement.getLink(),
                    true,
                    agreement.getText());
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    //Enable all the Fields(Views) which were disabled
    private void setElementsEnabled(){
        createButtonEnabled();
        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(true);
        }
    }

    //Disable the Fields(Views) when doing server call
    private void setElementsDisabled(){
        createButtonDisabled();
        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(false);
        }
    }

    //Disable the Create button during server call
    private void createButtonDisabled() {
        createAccountBtn.setBackgroundResource(R.drawable.new_bt_signin_active);
        createAccountBtn.setEnabled(false);
        createAccountTv.setText(getString(R.string.creating_account_text));
    }

    //Enable the Create button during server call
    private void createButtonEnabled() {
        createAccountBtn.setBackgroundResource(R.drawable.bt_signin_active);
        createAccountBtn.setEnabled(true);
        createAccountTv.setText(getString(R.string.create_account_text));
    }

    private void setupRegistrationForm() {
        try {
            RegistrationDescription form = new Api(this).getRegistrationDescription();

            LayoutInflater inflater = getLayoutInflater();

            List<RegistrationFormField> agreements = new ArrayList<>();

            for (RegistrationFormField field : form.getFields()) {
                if (field.getFieldType().equals(RegistrationFieldType.CHECKBOX)
                        && field.getAgreement() != null) {
                    // this is agreement field
                    // this must be added at the end of the form
                    // hold on it
                    agreements.add(field);
                }
                else {
                    IRegistrationFieldView fieldView = IRegistrationFieldView.Factory.getInstance(inflater, field);
                    if (fieldView != null) mFieldViews.add(fieldView);
                }
            }

            // add required and optional fields to the window
            for (IRegistrationFieldView v : mFieldViews) {
                if (v.getField().isRequired()) {
                    requiredFieldsLayout.addView(v.getView());
                }
                else {
                    optionalFieldsLayout.addView(v.getView());
                }
            }

            // add agreement fields to the window if available
            for (RegistrationFormField agreement : agreements) {
                IRegistrationFieldView agreementView = IRegistrationFieldView.Factory.getInstance(inflater, agreement);
                agreementView.setActionListener(new IRegistrationFieldView.IActionListener() {
                    @Override
                    public void onClickAgreement(RegistrationAgreement agreement) {
                        showAgreement(agreement);
                    }
                });
                agreementLayout.addView(agreementView.getView());
            }

            // request rendering of the layouts
            requiredFieldsLayout.requestLayout();
            optionalFieldsLayout.requestLayout();
            agreementLayout.requestLayout();

            // enable all the views
            setElementsEnabled();
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void createAccount() {
        if(!AppConstants.offline_flag){
            ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);

            boolean hasError = false;
            // prepare query (POST body)
            Bundle parameters = new Bundle();
            for (IRegistrationFieldView v : mFieldViews) {
                if (v.isValidInput()) {
                    if (v.hasValue()) {
                        // we submit the field only if it provides a value
                        parameters.putString(v.getField().getName(), v.getCurrentValue().getAsString());
                    }
                }
                else {
                    if (!hasError) {
                        // this is the first input field with error, so focus on it
                        scrollToView(scrollView, v.getView());
                    }
                    hasError = true;
                }
            }

            // set honor_code and terms_of_service to true
            parameters.putString("honor_code", "true");
            parameters.putString("terms_of_service", "true");

            //set parameter required by social registration
            PrefManager pref = new PrefManager(this, PrefManager.Pref.LOGIN);
            String access_token = pref.getString(PrefManager.Key.AUTH_TOKEN_SOCIAL);
            if ( access_token != null && access_token.length() > 0 ) {
                String backstore = pref.getString(PrefManager.Key.AUTH_TOKEN_BACKEND);
                parameters.putString("access_token", access_token);
                parameters.putString("provider", backstore);
                parameters.putString("client_id", Config.getInstance().getOAuthClientId());
            }


            // do NOT proceed if validations are failed
            if (hasError) {  return;  }

            try {
                //Send app version in create event
                String versionName = PropertyUtil.getManifestVersionName(this);
                String appVersion = String.format("%s v%s", getString(R.string.android), versionName);

                segIO.trackCreateAccountClicked(appVersion);
            }catch(Exception e){
                logger.error(e);
            }

            setElementsDisabled();
            showProgress();

            RegisterTask task = new RegisterTask(this, parameters) {

                @Override
                public void onFinish(RegisterResponse result) {
                    if(result!=null){
                        logger.debug("registration success=" + result.isSuccess());
                        setElementsEnabled();
                        hideProgress();

                        if ( !result.isSuccess()) {
                            FormFieldMessageBody messageBody = result.getMessageBody();
                            // show general failure message if there wasn't any error for any of the input fields
                            if (messageBody == null || messageBody.isEmpty()) {
                                String errorMessage = result.getValue();
                                if (errorMessage == null || errorMessage.isEmpty()) {
                                    errorMessage = getString(R.string.sign_up_error);
                                }
                                sendBroadcastFlyingErrorMessage(null, errorMessage);
                                return;
                            }

                            for(String key : messageBody.keySet()) {
                                if ( key == null )
                                    continue;
                                for (IRegistrationFieldView fieldView : mFieldViews) {
                                    if (key.equalsIgnoreCase( fieldView.getField().getName()) ) {
                                        List<RegisterResponseFieldError> error = messageBody.get(key);
                                        showErrorOnField(error, fieldView);
                                        break;
                                    }
                                }
                            }

                        } else {
                            AuthResponse auth = getAuth();
                            if (auth != null && auth.isSuccess()) {
                                //show different message based on server side status.
                                //or maybe server side return the detailed message?
                                RegisterResponse social = null;
                                PrefManager pref = new PrefManager(RegisterActivity.this, PrefManager.Pref.LOGIN);
                                String socialToken = pref.getString(PrefManager.Key.AUTH_TOKEN_BACKEND);
                                String message = "";
                                if ( socialToken != null ){
                                    if ( result.getStatus() == RegisterResponse.Status.EXISTING_ACCOUNT_LINKED ){
                                        message = "You have successfully linked with existing account!";
                                    } else if ( result.getStatus() == RegisterResponse.Status.EXISTING_ACCOUNT_NOT_LINKED ){
                                        message = "You have successfully created edX account!";
                                    } else if ( result.getStatus() == RegisterResponse.Status.NEW_ACCOUNT ){
                                        message = "You have successfully created edX account!";
                                    }
                                } else {
                                    message = "You have successfully created edX account!";
                                }

                                // launch my courses screen
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show(); 
                                Router.getInstance().showMyCourses(RegisterActivity.this);
                                finish();
                            } else {
                                sendBroadcastFlyingErrorMessage(null, getString(R.string.sign_up_error));
                            }
                        }
                    }else{
                        setElementsEnabled();
                        hideProgress();
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                    setElementsEnabled();
                    hideProgress();
                }
            };
            task.execute();
        }else {
            sendBroadcastFlyingErrorMessage(getString(R.string.no_connectivity),getString(R.string.network_not_connected));
        }
    }

    /**
     * Displays given errors on the given registration field.
     * @param errors
     * @param fieldView
     * @return
     */
    private void showErrorOnField(List<RegisterResponseFieldError> errors, IRegistrationFieldView fieldView) {
        if (errors != null && !errors.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (RegisterResponseFieldError e : errors) {
                buffer.append(e.getUserMessage() + " ");
            }

            fieldView.handleError(buffer.toString());
        }
    }

    /**
     * Scrolls to the top of the given View in the given ScrollView.
     * @param scrollView
     * @param view
     */
    public static void scrollToView(final ScrollView scrollView, final View view) {

        // View needs a focus
        view.requestFocus();

        // Determine if scroll needs to happen
        final Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (!view.getLocalVisibleRect(scrollBounds)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0, view.getTop());
                }
            });
        }
    }

    private void showProgress() {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        createAccountTv.setText(getString(R.string.creating_account_text));
    }

    private void hideProgress() {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        createAccountTv.setText(getString(R.string.create_account_text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Launch screen doesn't have any menu
        return true;
    }

    /**
     * we can create enum for strong type, but lose the extensibility.
     * @param socialType
     */
    private void showRegularMessage(int socialType){
        LinearLayout messageLayout = (LinearLayout) findViewById(R.id.message_layout);
        ETextView messageView = (ETextView) findViewById(R.id.message_body);
        //we replace facebook and google programmatically here
        //in order to make localization work
        String socialTypeString = "";
        String signUpSuccessString = "";
        if ( socialType == SocialFactory.TYPE_FACEBOOK ){
            socialTypeString = getString(R.string.facebook_text);
            signUpSuccessString = getString(R.string.sign_up_with_facebook_ok);
        } else {  //google
            socialTypeString = getString(R.string.google_text);
            signUpSuccessString = getString(R.string.sign_up_with_google_ok);
        }
        StringBuilder sb = new StringBuilder();
        sb.append( signUpSuccessString.replace(socialTypeString, "<b><strong>" + socialTypeString + "</strong></b>") )
            .append("<br>").append( getString(R.string.sign_up_with_social_ok) );

        Spanned result = Html.fromHtml(sb.toString());
        messageView.setText(result);
        messageLayout.setVisibility(View.VISIBLE);
       // UiUtil.animateLayouts(messageLayout);
    }

    private void updateUIOnSocialLoginToEdxFailure(int socialType){
        //change UI.
        View signupWith = findViewById(R.id.signupWith);
        signupWith.setVisibility(View.INVISIBLE);
        View socialPanel = findViewById(R.id.panel_social_layout);
        socialPanel.setVisibility(View.INVISIBLE);
        ETitleRowView signupWithEmailTitle = (ETitleRowView)findViewById(R.id.signupWithEmailTitle);
        signupWithEmailTitle.setTitle( getString(R.string.complete_registration) );
        //help method
        showRegularMessage(socialType);
        //populate the field with value from social site
        populateEmailFromSocialSite(socialType);

    }

    protected void populateFormField(String fieldName, String value){
        for (IRegistrationFieldView field : this.mFieldViews ){
            if ( fieldName.equalsIgnoreCase(field.getField().getName()) ) {
                boolean success = field.setRawValue(value);
                if ( success )
                    break;
            }
        }
    }




    private void populateEmailFromSocialSite(int socialType){
        this.socialLoginDelegate.getUserInfo(socialType, new SocialLoginDelegate.SocialUserInfoCallback() {
            @Override
            public void setSocialUserInfo(String email, String name) {
                populateFormField("email", email);
                if ( name != null && name.length() > 0 ) {
                    populateFormField("name", name);
                    populateFormField("username", name.replace(" ", "") + new Random(System.currentTimeMillis()).nextInt(9999));

                    //Should we save the email here?
                    PrefManager pref = new PrefManager(RegisterActivity.this, PrefManager.Pref.LOGIN);
                    pref.put("email", email);
                    pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, "none");
                }
            }
        });
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

    ///////section related to social login ///////////////
    // there are some duplicated code from login activity, as the logic
    //between login and registration is different subtly

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socialLoginDelegate.onActivityDestroyed();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       // outState.putString("username", email_et.getText().toString().trim());
       socialLoginDelegate.onActivitySaveInstanceState(outState);

    }
    @Override
    protected void onStop() {
        super.onStop();
        socialLoginDelegate.onActivityStopped();
    }
    @Override
    protected void onStart() {
        super.onStart();
//        if(email_et.getText().toString().length()==0){
//            displayLastEmailId();
//        }
        socialLoginDelegate.onActivityStarted();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *   after login by Facebook or Google, the workflow is different from login page.
     *   we need to adjust the register view
     *   1. first we try to login,
     *   2. if login return 200, redirect to course screen.
     *   3. otherwise, go through the normal registration flow.
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend,  Task task) {
        //we should handle UI update here. but right now we do nothing in UI
    }

    /*
     *  callback if login to edx success using social access_token
     */
    public void onUserLoginSuccess(ProfileModel profile) throws LoginException {

        PrefManager pref = new PrefManager(RegisterActivity.this, PrefManager.Pref.LOGIN);
        segIO.identifyUser(profile.id.toString(), profile.email , "");

        String backendKey = pref.getString(PrefManager.Key.SEGMENT_KEY_BACKEND);
        if(backendKey!=null){
            segIO.trackUserLogin(backendKey);
        }

        // but finish this screen anyways as login is succeeded
        finish();

        if (isActivityStarted()) {
            // do NOT launch next screen if app minimized
            Router.getInstance().showMyCourses(this);
        }

    }

    /**
     * callback if login to edx failed using social access_token
     * @param ex
     */
    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        //we should redirect to current page.
        //do nothing
        //we need to add 1)access_token   2) provider 3) client_id
        // handle if this is a LoginException

        logger.error(ex);
        int socialType = SocialFactory.getSocialType(backend);
        updateUIOnSocialLoginToEdxFailure(socialType);

    }

//    private class RegisterUsingSocialTokenTask extends Task<ProfileModel> {
//
//        public RegisterUsingSocialTokenTask(Context context) {
//            super(context);
//        }
//
//        @Override
//        public void onFinish(ProfileModel result) {
//            if (result != null) {
//                try {
//                    onRegisterUsingSoicalTokenSuccess(result);
//                } catch (LoginException ex) {
//                    logger.error(ex);
//                    handle(ex);
//                }
//            }
//        }
//
//        @Override
//        public void onException(Exception ex) {
//            onRegisterUsingSocialTokenFailure(ex);
//        }
//
//        @Override
//        protected ProfileModel doInBackground(Object... params) {
//            try {
//                String accessToken = (String) params[0];
//                String backend = (String) params[1];
//
//                Api api = new Api(context);
//
//                // do SOCIAL LOGIN first
//                RegisterResponse social = null;
//                if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_FACEBOOK)) {
//                    social = api.registerByFaceBook(accessToken);
//
//                    if ( social.getStatus() == RegisterResponse.Status.ERROR ) {
//                        throw new LoginException(new LoginErrorMessage(
//                                context.getString(R.string.error_account_not_linked_title_fb),
//                                context.getString(R.string.error_account_not_linked_desc_fb)));
//                    }
//                } else if (backend.equalsIgnoreCase(PrefManager.Value.BACKEND_GOOGLE)) {
//                    social = api.registerByFaceBook(accessToken);
//
//                    if ( social.getStatus() == RegisterResponse.Status.ERROR ) {
//                        throw new LoginException(new LoginErrorMessage(
//                                getString(R.string.error_account_not_linked_title_google),
//                                getString(R.string.error_account_not_linked_desc_google)));
//                    }
//                }
//                    // we got a valid accessToken so profile can be fetched
//                    ProfileModel profile =  api.getProfile();
//
//                    // store profile json
//                    if (profile != null ) {
//                        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
//                        pref.put(PrefManager.Key.PROFILE_JSON,  profile.json);
//                        pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, null);
//                        pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, null);
//                    }
//
//                    if (profile.email != null) {
//                        // we got valid profile information
//                        return profile;
//                    }
//
//                throw new LoginException(new LoginErrorMessage(
//                        getString(R.string.login_error),
//                        getString(R.string.login_failed)));
//            } catch (Exception e) {
//                logger.error(e);
//                handle(e);
//            }
//            return null;
//        }
//
//    }

    android.view.View.OnClickListener facebookClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (AppConstants.offline_flag) {
                showErrorMessage(getString(R.string.no_connectivity),
                        getString(R.string.network_not_connected));
            } else {
                Task<Void> logout = new Task<Void>(RegisterActivity.this) {

                    @Override
                    protected Void doInBackground(Object... arg0) {
                        try {
                            socialLoginDelegate.socialLogout(SocialFactory.TYPE_FACEBOOK);
                        } catch(Exception ex) {
                            // no need to handle this error
                            logger.error(ex);
                        }
                        return null;
                    }

                    @Override
                    public void onFinish(Void result) {
                        socialLoginDelegate.socialLogin(SocialFactory.TYPE_FACEBOOK);
                    }

                    @Override
                    public void onException(Exception ex) {

                    }
                };
                logout.execute();
            }
        }
    };

    android.view.View.OnClickListener googleClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (AppConstants.offline_flag) {
                showErrorMessage(getString(R.string.no_connectivity),
                        getString(R.string.network_not_connected));
            } else {
                Task<Void> logout = new Task<Void>(RegisterActivity.this) {

                    @Override
                    protected Void doInBackground(Object... arg0) {
                        try {
                            socialLoginDelegate.socialLogout(SocialFactory.TYPE_GOOGLE);
                        } catch(Exception ex) {
                            // no need to handle this error
                            logger.error(ex);
                        }
                        return null;
                    }

                    @Override
                    public void onFinish(Void result) {
                        socialLoginDelegate.socialLogin(SocialFactory.TYPE_GOOGLE);
                    }

                    @Override
                    public void onException(Exception ex) {

                    }
                };
                logout.execute();
            }
        }
    };

}
