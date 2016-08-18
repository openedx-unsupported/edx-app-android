package org.edx.mobile.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponseFieldError;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationAgreement;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;
import org.edx.mobile.module.registration.view.IRegistrationFieldView;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.RegisterTask;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.util.IntentFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class RegisterActivity extends BaseFragmentActivity
        implements SocialLoginDelegate.MobileLoginCallback {

    private ViewGroup createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private LinearLayout agreementLayout;
    private TextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();
    private SocialLoginDelegate socialLoginDelegate;
    private View facebookButton;
    private View googleButton;

    @Inject
    LoginPrefs loginPrefs;

    @NonNull
    public static Intent newIntent() {
        return IntentFactory.newIntentForComponent(RegisterActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle(ResourceUtil.getFormattedString(getResources(), R.string.register_title, "platform_name", environment.getConfig().getPlatformName()));

        environment.getSegment().trackScreenView(ISegment.Screens.LAUNCH_ACTIVITY);

        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this, environment.getConfig(), loginPrefs);

        boolean isSocialEnabled = false;
        facebookButton = findViewById(R.id.facebook_button);
        googleButton = findViewById(R.id.google_button);

        if (!SocialFactory.isSocialFeatureEnabled(getApplication(), SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK, environment.getConfig())) {
            facebookButton.setVisibility(View.GONE);
        } else {
            isSocialEnabled = true;
            facebookButton.setOnClickListener(socialLoginDelegate.createSocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK));
        }

        if (!SocialFactory.isSocialFeatureEnabled(getApplication(), SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE, environment.getConfig())) {
            googleButton.setVisibility(View.GONE);
        } else {
            isSocialEnabled = true;
            googleButton.setOnClickListener(socialLoginDelegate.createSocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE));
        }
        if (!isSocialEnabled) {
            findViewById(R.id.panel_social_layout).setVisibility(View.GONE);
            findViewById(R.id.or_signup_with_email_title).setVisibility(View.GONE);
            findViewById(R.id.signup_with_row).setVisibility(View.GONE);
        }

        TextView agreementMessageView = (TextView) findViewById(R.id.by_creating_account_tv);
        agreementMessageView.setText(R.string.by_creating_account);

        createAccountBtn = (ViewGroup) findViewById(R.id.createAccount_button_layout);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        createAccountTv = (TextView) findViewById(R.id.create_account_tv);
        requiredFieldsLayout = (LinearLayout) findViewById(R.id.required_fields_layout);
        optionalFieldsLayout = (LinearLayout) findViewById(R.id.optional_fields_layout);
        agreementLayout = (LinearLayout) findViewById(R.id.layout_agreement);
        final TextView optional_text = (TextView) findViewById(R.id.optional_field_tv);
        optional_text.setTextColor(optional_text.getLinkTextColors().getDefaultColor());
        optional_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (optionalFieldsLayout.getVisibility() == View.VISIBLE) {
                    optionalFieldsLayout.setVisibility(v.GONE);
                    optional_text.setText(getString(R.string.show_optional_text));
                } else {
                    optionalFieldsLayout.setVisibility(v.VISIBLE);
                    optional_text.setText(getString(R.string.hide_optional_text));
                }
            }
        });

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
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (isInAppEULALink) {
            // show EULA license that is shipped with app
            environment.getRouter().showWebViewDialog(this, getString(R.string.eula_file_link), getString(R.string.end_user_title));
        } else {
            // for any other link, open agreement link in a webview container
            environment.getRouter().showWebViewDialog(this, agreement.getLink(), agreement.getText());
        }
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
                } else {
                    IRegistrationFieldView fieldView = IRegistrationFieldView.Factory.getInstance(inflater, field);
                    if (fieldView != null) mFieldViews.add(fieldView);
                }
            }

            // add required and optional fields to the window
            for (IRegistrationFieldView v : mFieldViews) {
                if (v.getField().isRequired()) {
                    requiredFieldsLayout.addView(v.getView());
                } else {
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
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void createAccount() {
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
            } else {
                if (!hasError) {
                    showErrorPopup();
                }
                hasError = true;
            }
        }

        // set honor_code and terms_of_service to true
        parameters.putString("honor_code", "true");
        parameters.putString("terms_of_service", "true");

        //set parameter required by social registration
        final String access_token = loginPrefs.getSocialLoginAccessToken();
        final String backstore = loginPrefs.getSocialLoginProvider();
        boolean fromSocialNet = !TextUtils.isEmpty(access_token);
        if (fromSocialNet) {
            parameters.putString("access_token", access_token);
            parameters.putString("provider", backstore);
            parameters.putString("client_id", environment.getConfig().getOAuthClientId());
        }


        // do NOT proceed if validations are failed
        if (hasError) {
            return;
        }

        try {
            //Send app version in create event
            String versionName = BuildConfig.VERSION_NAME;
            String appVersion = String.format("%s v%s", getString(R.string.android), versionName);

            environment.getSegment().trackCreateAccountClicked(appVersion, backstore);
        } catch (Exception e) {
            logger.error(e);
        }

        showProgress();

        final SocialFactory.SOCIAL_SOURCE_TYPE backsourceType = SocialFactory.SOCIAL_SOURCE_TYPE.fromString(backstore);
        final RegisterTask task = new RegisterTask(this, parameters, access_token, backsourceType) {
            @Override
            public void onSuccess(AuthResponse auth) {
                onUserLoginSuccess(auth.profile);
            }

            @Override
            public void onException(Exception ex) {
                hideProgress();
                if (ex instanceof LoginAPI.RegistrationException) {
                    final FormFieldMessageBody messageBody = ((LoginAPI.RegistrationException) ex).getFormErrorBody();
                    boolean fieldErrorShown = false;
                    for (String key : messageBody.keySet()) {
                        if (key == null)
                            continue;
                        for (IRegistrationFieldView fieldView : mFieldViews) {
                            if (key.equalsIgnoreCase(fieldView.getField().getName())) {
                                List<RegisterResponseFieldError> error = messageBody.get(key);
                                showErrorOnField(error, fieldView);
                                fieldErrorShown = true;
                                break;
                            }
                        }
                    }
                    if (fieldErrorShown) {
                        // We are showing an error message on a visible form field.
                        return; // Return here to avoid showing the generic error pop-up.
                    }
                }
                RegisterActivity.this.showErrorDialog(null, ErrorUtils.getErrorMessage(ex, RegisterActivity.this));
            }
        };
        task.execute();
    }

    /**
     * Displays given errors on the given registration field.
     *
     * @param errors
     * @param fieldView
     * @return
     */
    private void showErrorOnField(List<RegisterResponseFieldError> errors, @NonNull IRegistrationFieldView fieldView) {
        if (errors != null && !errors.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (RegisterResponseFieldError e : errors) {
                buffer.append(e.getUserMessage() + " ");
            }
            showErrorPopup();
        }
    }

    private void showErrorPopup() {
        showErrorDialog(getResources().getString(R.string.registration_error_title), getResources().getString(R.string.registration_error_message), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
                View firstVisible = getFirstVisibleRegistrationField();
                if (scrollView!=null && firstVisible!=null) {
                    scrollToView(scrollView, firstVisible);
                }
            }
        });
    }

    @Nullable
    private View getFirstVisibleRegistrationField() {
        View view = getFirstVisibleFieldView(requiredFieldsLayout);
        if (view == null) getFirstVisibleFieldView(optionalFieldsLayout);
        return view;
    }

    @Nullable
    private View getFirstVisibleFieldView(@Nullable LinearLayout layout) {
        if (layout!=null) {
            for(int i=0; i<layout.getChildCount(); ++i) {
                View child=layout.getChildAt(i);
                if (child!=null && child.getVisibility()==View.VISIBLE)
                    return child;
            }
        }

        return null;
    }

    /**
     * Scrolls to the top of the given View in the given ScrollView.
     *
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
                    view.requestFocus();
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
     *
     * @param socialType
     */
    private void showRegularMessage(SocialFactory.SOCIAL_SOURCE_TYPE socialType) {
        LinearLayout messageLayout = (LinearLayout) findViewById(R.id.message_layout);
        TextView messageView = (TextView) findViewById(R.id.message_body);
        //we replace facebook and google programmatically here
        //in order to make localization work
        String socialTypeString = "";
        String signUpSuccessString = "";
        if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK) {
            socialTypeString = getString(R.string.facebook_text);
            signUpSuccessString = getString(R.string.sign_up_with_facebook_ok);
        } else {  //google
            socialTypeString = getString(R.string.google_text);
            signUpSuccessString = getString(R.string.sign_up_with_google_ok);
        }
        StringBuilder sb = new StringBuilder();
        CharSequence extraInfoPrompt = ResourceUtil.getFormattedString(getResources(), R.string.sign_up_with_social_ok, "platform_name", environment.getConfig().getPlatformName());
        sb.append(signUpSuccessString.replace(socialTypeString, "<b><strong>" + socialTypeString + "</strong></b>"))
                .append("<br>").append(extraInfoPrompt);

        Spanned result = Html.fromHtml(sb.toString());
        messageView.setText(result);
        messageLayout.setVisibility(View.VISIBLE);
        // UiUtil.animateLayouts(messageLayout);
    }

    private void updateUIOnSocialLoginToEdxFailure(SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken) {
        //change UI.
        View signupWith = findViewById(R.id.signup_with_row);
        signupWith.setVisibility(View.GONE);
        View socialPanel = findViewById(R.id.panel_social_layout);
        socialPanel.setVisibility(View.GONE);
        TextView signupWithEmailTitle = (TextView) findViewById(R.id.or_signup_with_email_title);
        signupWithEmailTitle.setText(getString(R.string.complete_registration));
        //help method
        showRegularMessage(socialType);
        //populate the field with value from social site
        populateEmailFromSocialSite(socialType, accessToken);
        //hide email and password field
        for (IRegistrationFieldView field : this.mFieldViews) {
            String fieldname = field.getField().getName();
            if ("password".equalsIgnoreCase(fieldname)) {
                field.getView().setVisibility(View.GONE);
                this.mFieldViews.remove(field);
                break;
            }
        }
        // registrationLayout.requestLayout();
    }

    protected void populateFormField(String fieldName, String value) {
        for (IRegistrationFieldView field : this.mFieldViews) {
            if (fieldName.equalsIgnoreCase(field.getField().getName())) {
                boolean success = field.setRawValue(value);
                if (success)
                    break;
            }
        }
    }


    private void populateEmailFromSocialSite(SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken) {
        this.socialLoginDelegate.getUserInfo(socialType, accessToken, new SocialLoginDelegate.SocialUserInfoCallback() {
            @Override
            public void setSocialUserInfo(String email, String name) {
                populateFormField("email", email);
                if (name != null && name.length() > 0) {
                    populateFormField("name", name);

                    //Should we save the email here?
                    loginPrefs.setLastAuthenticatedEmail(email);
                }
            }
        });
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
     * after login by Facebook or Google, the workflow is different from login page.
     * we need to adjust the register view
     * 1. first we try to login,
     * 2. if login return 200, redirect to course screen.
     * 3. otherwise, go through the normal registration flow.
     *
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend, Task task) {
        //we should handle UI update here. but right now we do nothing in UI
    }

    /*
     *  callback if login to edx success using social access_token
     */
    public void onUserLoginSuccess(ProfileModel profile) {
        setResult(RESULT_OK);
        finish();
    }

    /**
     * callback if login to edx failed using social access_token
     */
    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        // FIXME: We are assuming that if we get here, the accessToken is valid. That may not be the case!

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
        View progress = findViewById(R.id.progress_indicator);
        progress.setVisibility(View.VISIBLE);
        createAccountTv.setText(getString(R.string.creating_account_text));
    }

    private void hideProgress() {
        tryToSetUIInteraction(true);
        View progress = findViewById(R.id.progress_indicator);
        progress.setVisibility(View.GONE);
        createAccountTv.setText(getString(R.string.create_account_text));
    }


    //Disable the Create button during server call
    private void createButtonDisabled() {
        createAccountBtn.setEnabled(false);
        createAccountTv.setText(getString(R.string.create_account_text));
    }

    //Enable the Create button during server call
    private void createButtonEnabled() {
        createAccountBtn.setEnabled(true);
        createAccountTv.setText(getString(R.string.create_account_text));
    }


    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        if (enable) {
            unblockTouch();
            createButtonEnabled();
        } else {
            blockTouch();
            createButtonDisabled();
        }

        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(enable);
        }

        facebookButton.setClickable(enable);
        googleButton.setClickable(enable);

        return true;
    }
}
