package org.edx.mobile.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponseFieldError;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;
import org.edx.mobile.module.registration.view.IRegistrationFieldView;
import org.edx.mobile.module.registration.view.RegistrationSelectView;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.RegisterTask;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.custom.DividerWithTextView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;

public class RegisterActivity extends BaseFragmentActivity
        implements SocialLoginDelegate.MobileLoginCallback {
    private static final int ACCESSIBILITY_FOCUS_DELAY_MS = 500;

    private ViewGroup createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private TextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();
    private SocialLoginDelegate socialLoginDelegate;
    private View loadingIndicator;
    private View registrationForm;
    private View facebookButton;
    private View googleButton;
    private View microsoftButton;
    private TextView errorTextView;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    private LoginService loginService;

    @NonNull
    public static Intent newIntent() {
        return IntentFactory.newIntentForComponent(RegisterActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        super.setToolbarAsActionBar();

        setTitle(R.string.register_title);

        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.REGISTER);

        loadingIndicator = findViewById(R.id.loadingIndicator);
        registrationForm = findViewById(R.id.registration_form);

        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState,
                this, environment.getConfig(), loginPrefs, SocialLoginDelegate.Feature.REGISTRATION);

        errorTextView = (TextView) findViewById(R.id.content_unavailable_error_text);

        boolean isSocialEnabled = false;
        facebookButton = findViewById(R.id.facebook_button);
        googleButton = findViewById(R.id.google_button);
        microsoftButton = findViewById(R.id.microsoft_button);

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

        if (!SocialFactory.isSocialFeatureEnabled(getApplicationContext(), SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT, environment.getConfig())) {
            microsoftButton.setVisibility(View.GONE);
        } else {
            isSocialEnabled = true;
            microsoftButton.setOnClickListener(socialLoginDelegate
                    .createSocialButtonClickHandler(SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT));
        }

        if (!isSocialEnabled) {
            findViewById(R.id.panel_social_layout).setVisibility(View.GONE);
            findViewById(R.id.or_signup_with_email_title).setVisibility(View.GONE);
            findViewById(R.id.signup_with_row).setVisibility(View.GONE);
        }

        TextView agreementMessageView = (TextView) findViewById(R.id.by_creating_account_tv);
        agreementMessageView.setMovementMethod(LinkMovementMethod.getInstance());
        agreementMessageView.setText(org.edx.mobile.util.TextUtils.generateLicenseText(
                environment.getConfig(), getResources(), R.string.by_creating_account));

        createAccountBtn = (ViewGroup) findViewById(R.id.createAccount_button_layout);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoftKeyboardUtil.hide(RegisterActivity.this);
                validateRegistrationFields();
            }
        });

        createAccountTv = (TextView) findViewById(R.id.create_account_tv);
        requiredFieldsLayout = (LinearLayout) findViewById(R.id.required_fields_layout);
        optionalFieldsLayout = (LinearLayout) findViewById(R.id.optional_fields_layout);
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

        getRegistrationForm();

        hideSoftKeypad();
        tryToSetUIInteraction(true);
    }

    private void validateRegistrationFields() {
        Bundle parameters = getRegistrationParameters();
        if (parameters == null) {
            return;
        }
        showProgress();
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Call<JsonObject> validateRegistrationFields = loginService.validateRegistrationFields(parameterMap);
        validateRegistrationFields.enqueue(new ErrorHandlingCallback<JsonObject>(this) {
            @Override
            protected void onResponse(@NonNull JsonObject responseBody) {
                // Callback method for a successful HTTP response.
                final Type stringMapType = new TypeToken<HashMap<String, String>>() {
                }.getType();
                final HashMap<String, String> messageBody = new Gson().fromJson(responseBody.get(ApiConstants.VALIDATION_DECISIONS), stringMapType);
                if (hasValidationError(messageBody)) {
                    hideProgress();
                } else {
                    createAccount(parameters);
                }
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                hideProgress();
                RegisterActivity.this.showAlertDialog(null, ErrorUtils.getErrorMessage(error,
                        RegisterActivity.this));
                logger.error(error);
            }
        });
    }

    private boolean hasValidationError(HashMap<String, String> messageBody) {
        boolean errorShown = false;
        for (String key : messageBody.keySet()) {
            if (key == null)
                continue;
            for (IRegistrationFieldView fieldView : mFieldViews) {
                String error = messageBody.get(key);
                if (!TextUtils.isEmpty(error) && key.equalsIgnoreCase(fieldView.getField().getName())) {
                    fieldView.handleError(error);
                    if (!errorShown) {
                        // this is the first input field with error,
                        // so focus on it after showing the popup
                        showErrorPopup(fieldView.getOnErrorFocusView());
                        errorShown = true;
                    }
                    break;
                }
            }
        }
        return errorShown;
    }

    private void showErrorMessage(String errorMsg, @NonNull Icon errorIcon) {
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(errorMsg);
        errorTextView.setCompoundDrawablesWithIntrinsicBounds(null,
                new IconDrawable(this, errorIcon)
                        .sizeRes(this, R.dimen.content_unavailable_error_icon_size)
                        .colorRes(this, R.color.neutralDark),
                null, null
        );
    }

    public void getRegistrationForm() {
        if (!NetworkUtil.isConnected(this)) {
            showErrorMessage(getString(R.string.reset_no_network_message),
                    FontAwesomeIcons.fa_wifi);
            return;
        }

        tryToSetUIInteraction(false);
        loadingIndicator.setVisibility(View.VISIBLE);

        final Call<RegistrationDescription> getRegistrationFormCall = loginService.getRegistrationForm(
                environment.getConfig().getApiUrlVersionConfig().getRegistrationApiVersion());
        getRegistrationFormCall.enqueue(new ErrorHandlingCallback<RegistrationDescription>(this) {
            @Override
            protected void onResponse(@NonNull RegistrationDescription registrationDescription) {
                updateUI(true);
                setupRegistrationForm(registrationDescription);
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                updateUI(false);
                showErrorMessage(ErrorUtils.getErrorMessage(error, RegisterActivity.this),
                        FontAwesomeIcons.fa_exclamation_circle);
                logger.error(error);
            }

            private void updateUI(boolean isSuccess) {
                tryToSetUIInteraction(true);
                registrationForm.setVisibility(isSuccess ? View.VISIBLE : View.GONE);
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void setupRegistrationForm(RegistrationDescription form) {
        LayoutInflater inflater = getLayoutInflater();

        for (RegistrationFormField field : form.getFields()) {
            IRegistrationFieldView fieldView = IRegistrationFieldView.Factory.getInstance(inflater, field);
            if (fieldView != null) mFieldViews.add(fieldView);
            // Add item selected listener for spinner views
            if (field.getFieldType().equals(RegistrationFieldType.MULTI)) {
                RegistrationSelectView selectView = (RegistrationSelectView) fieldView;
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

        // enable all the views
        tryToSetUIInteraction(true);
    }

    private void createAccount(Bundle parameters) {
        // set honor_code and terms_of_service to true
        parameters.putString("honor_code", "true");
        parameters.putString("terms_of_service", "true");

        //set parameter required by social registration
        final String access_token = loginPrefs.getSocialLoginAccessToken();
        final String provider = loginPrefs.getSocialLoginProvider();
        boolean fromSocialNet = !TextUtils.isEmpty(access_token);
        if (fromSocialNet) {
            parameters.putString("access_token", access_token);
            parameters.putString("provider", provider);
            parameters.putString("client_id", environment.getConfig().getOAuthClientId());
        }

        // Send analytics event for Create Account button click
        final String appVersion = String.format("%s v%s", getString(R.string.android), BuildConfig.VERSION_NAME);
        environment.getAnalyticsRegistry().trackCreateAccountClicked(appVersion, provider);

        final SocialFactory.SOCIAL_SOURCE_TYPE backsourceType = SocialFactory.SOCIAL_SOURCE_TYPE.fromString(provider);
        final RegisterTask task = new RegisterTask(this, parameters, access_token, backsourceType) {
            @Override
            public void onSuccess(AuthResponse auth) {
                environment.getAnalyticsRegistry().trackRegistrationSuccess(appVersion, provider);
                onUserLoginSuccess(auth.profile);
            }

            @Override
            public void onException(Exception ex) {
                hideProgress();
                if (ex instanceof LoginAPI.RegistrationException) {
                    final FormFieldMessageBody messageBody = ((LoginAPI.RegistrationException) ex).getFormErrorBody();
                    boolean errorShown = false;
                    for (String key : messageBody.keySet()) {
                        if (key == null)
                            continue;
                        for (IRegistrationFieldView fieldView : mFieldViews) {
                            if (key.equalsIgnoreCase(fieldView.getField().getName())) {
                                List<RegisterResponseFieldError> error = messageBody.get(key);
                                showErrorOnField(error, fieldView);
                                if (!errorShown) {
                                    // this is the first input field with error,
                                    // so focus on it after showing the popup
                                    showErrorPopup(fieldView.getOnErrorFocusView());
                                    errorShown = true;
                                }
                                break;
                            }
                        }
                    }
                    if (errorShown) {
                        // We have already shown a specific error message.
                        return; // Return here to avoid falling back to the generic error handler.
                    }
                }
                // If app version is un-supported
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UPGRADE_REQUIRED) {
                    RegisterActivity.this.showAlertDialog(null,
                            getString(R.string.app_version_unsupported_register_msg),
                            getString(R.string.label_update),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppStoreUtils.openAppInAppStore(RegisterActivity.this);
                                }
                            }, getString(android.R.string.cancel), null);
                } else {
                    RegisterActivity.this.showAlertDialog(null, ErrorUtils.getErrorMessage(ex, RegisterActivity.this));
                }
            }
        };
        task.execute();
    }

    private Bundle getRegistrationParameters() {
        boolean hasError = false;
        Bundle parameters = new Bundle();
        String email = null, confirm_email = null;
        for (IRegistrationFieldView v : mFieldViews) {
            if (v.isValidInput()) {
                if (v.getField().getName().equalsIgnoreCase(RegistrationFieldType.EMAIL.name())) {
                    email = v.getCurrentValue().getAsString();
                }
                if (v.getField().getName().equalsIgnoreCase(RegistrationFieldType.CONFIRM_EMAIL.name())) {
                    confirm_email = v.getCurrentValue().getAsString();
                }

                // Validating email field with confirm email field
                if (email != null && confirm_email != null && !email.equalsIgnoreCase(confirm_email)) {
                    v.handleError(v.getField().getErrorMessage().getRequired());
                    showErrorPopup(v.getOnErrorFocusView());
                    return null;
                }
                if (v.hasValue()) {
                    // we submit the field only if it provides a value
                    parameters.putString(v.getField().getName(), v.getCurrentValue().getAsString());
                }
            } else {
                if (!hasError) {
                    // this is the first input field with error,
                    // so focus on it after showing the popup
                    showErrorPopup(v.getOnErrorFocusView());
                }
                hasError = true;
            }
        }
        // do NOT proceed if validations are failed
        if (hasError) {
            return null;
        }
        return parameters;
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
            fieldView.handleError(buffer.toString());
        }
    }

    private void showErrorPopup(@NonNull final View errorView) {
        showAlertDialog(getResources().getString(R.string.registration_error_title), getResources().getString(R.string.registration_error_message), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scrollToView((ScrollView) findViewById(R.id.scrollview), errorView);
            }
        });
    }

    /**
     * Scrolls to the top of the given View in the given ScrollView.
     *
     * @param scrollView
     * @param view
     */
    public static void scrollToView(final ScrollView scrollView, final View view) {
        /*
        The delayed focus has been added so that TalkBack reads the proper view's description that
        we want focus on. For example in case of {@link RegistrationEditText} we want accessibility
        focus on TIL when an error is displayed instead of the EditText within it, which can only
        be achieved through this delay.
         */
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.requestFocus();
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
        }, ACCESSIBILITY_FOCUS_DELAY_MS);

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
        } else if (socialType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT) {
            socialTypeString = getString(R.string.microsoft_text);
            signUpSuccessString = getString(R.string.sign_up_with_microsoft_ok);
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
        DividerWithTextView signupWithEmailTitle = (DividerWithTextView) findViewById(R.id.or_signup_with_email_title);
        signupWithEmailTitle.setText(getString(R.string.complete_registration));
        //help method
        showRegularMessage(socialType);
        //populate the field with value from social site
        populateEmailFromSocialSite(socialType, accessToken);
        //hide confirm email and password field as we don't need them in case of social signup
        List<IRegistrationFieldView> extraFields = new ArrayList<>();
        for (IRegistrationFieldView field : this.mFieldViews) {
            String fieldName = field.getField().getName();
            if (RegistrationFieldType.CONFIRM_EMAIL.name().equalsIgnoreCase(fieldName) ||
                    RegistrationFieldType.PASSWORD.name().equalsIgnoreCase(fieldName)) {
                field.getView().setVisibility(View.GONE);
                extraFields.add(field);
            }
        }
        this.mFieldViews.removeAll(extraFields);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        if (ex instanceof HttpStatusException && ((HttpStatusException) ex).getStatusCode() == HttpStatus.FORBIDDEN) {
            RegisterActivity.this.showAlertDialog(getString(R.string.login_error),
                    getString(R.string.auth_provider_disabled_user_error),
                    getString(R.string.label_customer_support),
                    (dialog, which) -> environment.getRouter()
                            .showFeedbackScreen(RegisterActivity.this,
                                    getString(R.string.email_subject_account_disabled)), getString(android.R.string.cancel), null);
        } else {
            SocialFactory.SOCIAL_SOURCE_TYPE socialType = SocialFactory.SOCIAL_SOURCE_TYPE.fromString(backend);
            updateUIOnSocialLoginToEdxFailure(socialType, accessToken);
        }
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
        microsoftButton.setClickable(enable);

        return true;
    }
}
