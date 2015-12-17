package org.edx.mobile.view;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.event.FlyingMessageEvent;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.ProfileModel;
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
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class RegisterActivity extends BaseFragmentActivity
        implements SocialLoginDelegate.MobileLoginCallback {

    private RelativeLayout createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private LinearLayout agreementLayout;
    private LinearLayout registrationLayout;
    private ETextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();
    private SocialLoginDelegate socialLoginDelegate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        environment.getSegment().trackScreenView(ISegment.Screens.LAUNCH_ACTIVITY);

        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this, environment.getConfig());

        boolean isSocialEnabled = SocialFactory.isSocialFeatureEnabled(
            getApplicationContext(), SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_UNKNOWN, environment.getConfig());

        if ( !isSocialEnabled ){
            findViewById(R.id.panel_social_layout).setVisibility(View.GONE);
            findViewById(R.id.or_signup_with_email_title).setVisibility(View.GONE);
            findViewById(R.id.signup_with_row).setVisibility(View.GONE);
        }
        else {
            ImageView imgFacebook=(ImageView)findViewById(R.id.img_facebook);
            ImageView imgGoogle=(ImageView)findViewById(R.id.img_google);

            if ( !environment.getConfig().getFacebookConfig().isEnabled() ){
                findViewById(R.id.img_facebook).setVisibility(View.GONE);
            }
            else {
                imgFacebook.setClickable(true);
                imgFacebook.setOnClickListener( socialLoginDelegate.createSocialButtonClickHandler( SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK ) );
            }

            if ( !environment.getConfig().getGoogleConfig().isEnabled() ){
                findViewById(R.id.img_google).setVisibility(View.GONE);
            }
            else {
                imgGoogle.setClickable(true);
                imgGoogle.setOnClickListener( socialLoginDelegate.createSocialButtonClickHandler( SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE ) ) ;
             }
        }

        TextView agreementMessageView = (TextView)findViewById(R.id.by_creating_account_tv);
        CharSequence agreementMessage = ResourceUtil.getFormattedString(getResources(), R.string.by_creating_account, "platform_name", environment.getConfig().getPlatformName());
        agreementMessageView.setText(agreementMessage);

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
        final ETextView optional_text=(ETextView)findViewById(R.id.optional_field_tv);
        optional_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optionalFieldsLayout.getVisibility() == View.VISIBLE) {
                    optionalFieldsLayout.setVisibility(v.GONE);
                    optional_text.setText(getString(R.string.show_optional_text));
                }
                else{
                    optionalFieldsLayout.setVisibility(v.VISIBLE);
                    optional_text.setText(getString(R.string.hide_optional_text));
                }
            }
        });

        View closeButton = findViewById(R.id.actionbar_close_btn);
        if(closeButton!=null){
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if user cancel the registration, do the clean up
                    PrefManager pref = new PrefManager(RegisterActivity.this, PrefManager.Pref.LOGIN);
                    pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, null);
                    pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, null);

                    finish();
                }
            });
        }
        registrationLayout = (LinearLayout)findViewById(R.id.registrationLayout);

        ETextView customTitle = (ETextView) findViewById(R.id.activity_title);
        if(customTitle!=null){
            CharSequence title = ResourceUtil.getFormattedString(getResources(), R.string.register_title, "platform_name", environment.getConfig().getPlatformName());
            customTitle.setText(title);
        }

        setupRegistrationForm();
        hideSoftKeypad();
        tryToSetUIInteraction(true);

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


    private void setupRegistrationForm() {
        try {
            RegistrationDescription form = environment.getServiceManager().getRegistrationDescription();

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
            tryToSetUIInteraction(true);
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void createAccount() {
        if(NetworkUtil.isConnected(this)){
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
            String backstore = pref.getString(PrefManager.Key.AUTH_TOKEN_BACKEND);
            boolean fromSocialNet = !TextUtils.isEmpty(access_token);
            if ( fromSocialNet ) {
                parameters.putString("access_token", access_token);
                parameters.putString("provider", backstore);
                parameters.putString("client_id", environment.getConfig().getOAuthClientId());
            }


            // do NOT proceed if validations are failed
            if (hasError) {  return;  }

            try {
                //Send app version in create event
                String versionName = PropertyUtil.getManifestVersionName(this);
                String appVersion = String.format("%s v%s", getString(R.string.android), versionName);

                environment.getSegment().trackCreateAccountClicked(appVersion, backstore);
            }catch(Exception e){
                logger.error(e);
            }

            showProgress();

            SocialFactory.SOCIAL_SOURCE_TYPE backsourceType = SocialFactory.SOCIAL_SOURCE_TYPE.fromString(backstore);
            RegisterTask task = new RegisterTask(this, parameters, access_token, backsourceType) {

                @Override
                public void onSuccess(RegisterResponse result) {
                    if(result!=null){
                        logger.debug("registration success=" + result.isSuccess());
                        hideProgress();

                        if ( !result.isSuccess()) {
                            FormFieldMessageBody messageBody = result.getMessageBody();
                            // show general failure message if there wasn't any error for any of the input fields
                            if (messageBody == null || messageBody.isEmpty()) {
                                String errorMessage = result.getValue();
                                if (errorMessage == null || errorMessage.isEmpty()) {
                                    errorMessage = getString(R.string.sign_up_error);
                                }
                                EventBus.getDefault().postSticky(new FlyingMessageEvent(FlyingMessageEvent.MessageType.ERROR, null, errorMessage));
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
                                //in the future we will show different messages based on different registration
                                //condition
                                showProgress();
                                environment.getRouter().showMyCourses(RegisterActivity.this);
                                finish();
                            } else {
                                EventBus.getDefault().postSticky(
                                        new FlyingMessageEvent(FlyingMessageEvent.MessageType.ERROR, null, getString(R.string.sign_up_error)));
                            }
                        }
                    }else{
                        hideProgress();
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                    hideProgress();
                }
            };
            task.execute();
        }else {
            EventBus.getDefault().postSticky(
                    new FlyingMessageEvent(FlyingMessageEvent.MessageType.ERROR,
                            getString(R.string.no_connectivity),getString(R.string.network_not_connected)));
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


    @Override
    public boolean createOptionsMenu(Menu menu) {
        // Register screen doesn't have any menu
        return true;
    }

    /**
     * we can create enum for strong type, but lose the extensibility.
     * @param socialType
     */
    private void showRegularMessage(SocialFactory.SOCIAL_SOURCE_TYPE socialType){
        LinearLayout messageLayout = (LinearLayout) findViewById(R.id.message_layout);
        ETextView messageView = (ETextView) findViewById(R.id.message_body);
        //we replace facebook and google programmatically here
        //in order to make localization work
        String socialTypeString = "";
        String signUpSuccessString = "";
        if ( socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK ){
            socialTypeString = getString(R.string.facebook_text);
            signUpSuccessString = getString(R.string.sign_up_with_facebook_ok);
        } else {  //google
            socialTypeString = getString(R.string.google_text);
            signUpSuccessString = getString(R.string.sign_up_with_google_ok);
        }
        StringBuilder sb = new StringBuilder();
        CharSequence extraInfoPrompt = ResourceUtil.getFormattedString(getResources(), R.string.sign_up_with_social_ok, "platform_name", environment.getConfig().getPlatformName());
        sb.append( signUpSuccessString.replace(socialTypeString, "<b><strong>" + socialTypeString + "</strong></b>") )
            .append("<br>").append(extraInfoPrompt);

        Spanned result = Html.fromHtml(sb.toString());
        messageView.setText(result);
        messageLayout.setVisibility(View.VISIBLE);
       // UiUtil.animateLayouts(messageLayout);
    }

    private void updateUIOnSocialLoginToEdxFailure(SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken){
        //change UI.
        View signupWith = findViewById(R.id.signup_with_row);
        signupWith.setVisibility(View.GONE);
        View socialPanel = findViewById(R.id.panel_social_layout);
        socialPanel.setVisibility(View.GONE);
        ETextView signupWithEmailTitle = (ETextView)findViewById(R.id.or_signup_with_email_title);
        signupWithEmailTitle.setText( getString(R.string.complete_registration) );
        //help method
        showRegularMessage(socialType);
        //populate the field with value from social site
        populateEmailFromSocialSite(socialType, accessToken);
        //hide email and password field
        for (IRegistrationFieldView field : this.mFieldViews ){
            String fieldname = field.getField().getName();
            if ( "password".equalsIgnoreCase(fieldname) ) {
                 field.getView().setVisibility(View.GONE);
                 this.mFieldViews.remove(field);
                 break;
            }
        }
       // registrationLayout.requestLayout();
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




    private void populateEmailFromSocialSite(SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken){
        this.socialLoginDelegate.getUserInfo(socialType, accessToken, new SocialLoginDelegate.SocialUserInfoCallback() {
            @Override
            public void setSocialUserInfo(String email, String name) {
                populateFormField("email", email);
                if ( name != null && name.length() > 0 ) {
                    populateFormField("name", name);

                    //Should we save the email here?
                    PrefManager pref = new PrefManager(RegisterActivity.this, PrefManager.Pref.LOGIN);
                    pref.put("email", email);
                    pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, "none");
                }
            }
        });
    }

    @Override
    public boolean showErrorMessage(String header, String message, boolean isPersistent) {
        if (message != null) {
            return super.showErrorMessage(header, message, isPersistent);
        } else {
            return super.showErrorMessage(header, getString(R.string.login_failed), isPersistent);
        }
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
        tryToSetUIInteraction(true);
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
        environment.getSegment().identifyUser(profile.id.toString(), profile.email , "");

        String backendKey = pref.getString(PrefManager.Key.SEGMENT_KEY_BACKEND);
        if(backendKey!=null){
            environment.getSegment().trackUserLogin(backendKey);
        }


        if (isActivityStarted()) {
            // do NOT launch next screen if app minimized
            showProgress();
            environment.getRouter().showMyCourses(this);
        }
        // but finish this screen anyways as login is succeeded
        finish();
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
        tryToSetUIInteraction(true);
        logger.error(ex);
        SocialFactory.SOCIAL_SOURCE_TYPE socialType = SocialFactory.SOCIAL_SOURCE_TYPE.fromString(backend);
        updateUIOnSocialLoginToEdxFailure(socialType, accessToken);

    }

    //help functions for UI enable/disable states

    private void showProgress() {
        tryToSetUIInteraction(false);
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        createAccountTv.setText(getString(R.string.creating_account_text));
    }

    private void hideProgress() {
        tryToSetUIInteraction(true);
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        createAccountTv.setText(getString(R.string.create_account_text));
    }


    //Disable the Create button during server call
    private void createButtonDisabled() {
        createAccountBtn.setBackgroundResource(R.drawable.new_bt_signin_active);
        createAccountBtn.setEnabled(false);
        createAccountTv.setText(getString(R.string.create_account_text));
    }

    //Enable the Create button during server call
    private void createButtonEnabled() {
        createAccountBtn.setBackgroundResource(R.drawable.bt_signin_active);
        createAccountBtn.setEnabled(true);
        createAccountTv.setText(getString(R.string.create_account_text));
    }


    @Override
    public boolean tryToSetUIInteraction(boolean enable){
        if ( enable ){
            unblockTouch();
            createButtonEnabled();
        } else {
            blockTouch();
            createButtonDisabled();
        }

        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(enable);
        }

        ImageView imgFacebook=(ImageView)findViewById(R.id.img_facebook);
        ImageView imgGoogle=(ImageView)findViewById(R.id.img_google);
        imgFacebook.setClickable(enable);
        imgGoogle.setClickable(enable);

        return true;
    }

}
