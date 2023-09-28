package org.edx.mobile.view

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.ScrollView
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.BuildConfig
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginAPI.RegistrationException
import org.edx.mobile.authentication.LoginService
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.databinding.ActivityRegisterBinding
import org.edx.mobile.extenstion.isNotNullOrEmpty
import org.edx.mobile.extenstion.isVisible
import org.edx.mobile.extenstion.serializableOrThrow
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.callback.ErrorHandlingCallback
import org.edx.mobile.http.constants.ApiConstants
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.model.api.RegisterResponseFieldError
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.module.registration.model.RegistrationDescription
import org.edx.mobile.module.registration.model.RegistrationFieldType
import org.edx.mobile.module.registration.view.IRegistrationFieldView
import org.edx.mobile.module.registration.view.IRegistrationFieldView.Factory.getInstance
import org.edx.mobile.module.registration.view.IRegistrationFieldView.IActionListener
import org.edx.mobile.social.SocialAuthSource
import org.edx.mobile.social.SocialLoginDelegate
import org.edx.mobile.social.SocialLoginDelegate.Feature
import org.edx.mobile.social.SocialLoginDelegate.MobileLoginCallback
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.AppStoreUtils
import org.edx.mobile.util.ConfigUtil
import org.edx.mobile.util.IntentFactory
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.util.TextUtils
import org.edx.mobile.util.images.ErrorUtils
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.viewModel.AuthViewModel
import javax.inject.Inject


@AndroidEntryPoint
class RegisterActivity : BaseFragmentActivity(), MobileLoginCallback {

    private lateinit var binding: ActivityRegisterBinding
    private val mFieldViews: MutableList<IRegistrationFieldView> = mutableListOf()
    private lateinit var socialLoginDelegate: SocialLoginDelegate
    private lateinit var errorNotification: FullScreenErrorNotification
    private var socialRegistrationType = SocialAuthSource.UNKNOWN
    private var savedRegistrationFormState: Bundle? = null

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var loginPrefs: LoginPrefs

    @Inject
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initObservers()
        hideSoftKeypad()
        tryToSetUIInteraction(true)
        environment.analyticsRegistry.trackScreenView(Analytics.Screens.REGISTER)
    }

    private fun initViews() {
        setToolbarAsActionBar()
        setTitle(R.string.register_title)
        errorNotification = FullScreenErrorNotification(binding.scrollview)
        getRegistrationForm()
        binding.createAccountBtn.setOnClickListener {
            validateRegistrationFields()
        }
        binding.optionalFieldTv.setOnCheckedChangeListener { _, isChecked: Boolean ->
            binding.optionalFieldsLayout.setVisibility(isChecked)
        }
        setupSocialAuth()
        initEULA()
    }

    private fun initEULA() {
        binding.eulaTv.movementMethod = LinkMovementMethod.getInstance()
        binding.eulaTv.text = TextUtils.generateLicenseText(
            environment.config, this, R.string.by_creating_account
        )
    }

    private fun setupSocialAuth() {
        if (setupSocialLoginButton()) {
            socialLoginDelegate = SocialLoginDelegate(
                this,
                this,
                environment.config,
                environment.loginPrefs,
                Feature.REGISTRATION
            ).apply {
                binding.socialAuth.facebookButton.setOnClickListener(
                    createSocialButtonClickHandler(SocialAuthSource.FACEBOOK)
                )
                binding.socialAuth.googleButton.setOnClickListener(
                    createSocialButtonClickHandler(SocialAuthSource.GOOGLE)
                )
                binding.socialAuth.microsoftButton.setOnClickListener(
                    createSocialButtonClickHandler(SocialAuthSource.MICROSOFT)
                )
            }
        }
    }

    private fun setupSocialLoginButton(): Boolean {
        binding.socialAuth.apply {
            facebookButton.text =
                getString(R.string.continue_with_social, getString(R.string.facebook_text))
            facebookButton.setVisibility(isSocialFeatureEnable(SocialAuthSource.FACEBOOK))

            googleButton.text =
                getString(R.string.continue_with_social, getString(R.string.google_text))
            googleButton.setVisibility(isSocialFeatureEnable(SocialAuthSource.GOOGLE))

            microsoftButton.text =
                getString(R.string.continue_with_social, getString(R.string.microsoft_text))
            microsoftButton.setVisibility(isSocialFeatureEnable(SocialAuthSource.MICROSOFT))

            val isSocialLoginEnable =
                facebookButton.isVisible() || googleButton.isVisible() || microsoftButton.isVisible()
            root.setVisibility(isSocialLoginEnable)
            return isSocialLoginEnable
        }
    }

    private fun initObservers() {
        authViewModel.onRegister.observe(this, EventObserver {
            if (it) {
                val appVersion = "${getString(R.string.android)} ${BuildConfig.VERSION_NAME}"
                val provider = environment.loginPrefs.socialLoginProvider

                environment.analyticsRegistry.trackRegistrationSuccess(appVersion, provider)
                onUserLoginSuccess()
            }
        })

        authViewModel.errorMessage.observe(this, EventObserver { error ->
            setBtnProgressEnabled(false)
            val ex = error.throwable
            if (ex is RegistrationException) {
                val messageBody = ex.formErrorBody
                var errorShown = false
                for ((key, value) in messageBody) {
                    mFieldViews.find { fieldView ->
                        fieldView.getField().name.equals(key, true)
                    }?.let { fieldView ->
                        showErrorOnField(value, fieldView)
                        if (!errorShown) {
                            // this is the first input field with error,
                            // so focus on it after showing the popup
                            showErrorPopup(fieldView.getOnErrorFocusView())
                            errorShown = true
                        }
                    }
                }
                if (errorShown) {
                    // We have already shown a specific error message.
                    // Return here to avoid falling back to the generic error handler.
                    return@EventObserver
                }
            }
            // If app version is un-supported
            if (ex is HttpStatusException && ex.statusCode == HttpStatus.UPGRADE_REQUIRED) {
                showAlertDialog(
                    null,
                    getString(R.string.app_version_unsupported_register_msg),
                    getString(R.string.label_update),
                    { _, _ -> AppStoreUtils.openAppInAppStore(this@RegisterActivity) },
                    getString(android.R.string.cancel),
                    null
                )
            } else if (ex is HttpStatusException && ex.statusCode == HttpStatus.FORBIDDEN) {
                tryToSetUIInteraction(true)
                showAlertDialog(
                    getString(R.string.login_error),
                    getString(R.string.auth_provider_disabled_user_error),
                    getString(R.string.label_customer_support),
                    { _, _ ->
                        environment.router.showFeedbackScreen(
                            this, getString(R.string.email_subject_account_disabled)
                        )
                    },
                    getString(android.R.string.cancel), null
                )
            } else {
                showAlertDialog(null, ErrorUtils.getErrorMessage(ex, this@RegisterActivity))
            }
        })

        authViewModel.socialLoginErrorMessage.observe(this, EventObserver {
            tryToSetUIInteraction(true)
            logger.error(it.throwable)
            socialRegistrationType = SocialAuthSource.fromString(loginPrefs.socialLoginProvider)
            updateUIOnSocialLoginToEdxFailure(
                socialRegistrationType,
                loginPrefs.socialLoginAccessToken
            )
        })
    }

    private fun isSocialFeatureEnable(source: SocialAuthSource): Boolean {
        return ConfigUtil.isSocialFeatureEnabled(source, environment.config)
    }

    private fun validateRegistrationFields() {
        hideSoftKeypad()
        val parameters = getRegistrationParameters() ?: return

        setBtnProgressEnabled(true)
        val parameterMap: MutableMap<String, String?> = mutableMapOf()
        for (key in parameters.keySet()) {
            parameterMap[key] = parameters.getString(key)
        }
        val validateRegistrationFields = loginService.validateRegistrationFields(parameterMap)
        validateRegistrationFields.enqueue(object : ErrorHandlingCallback<JsonObject>(this) {
            override fun onResponse(responseBody: JsonObject) {
                // Callback method for a successful HTTP response.
                val stringMapType = object : TypeToken<HashMap<String, String?>?>() {}.type
                val messageBody = Gson().fromJson<HashMap<String, String>>(
                    responseBody[ApiConstants.VALIDATION_DECISIONS], stringMapType
                )
                if (hasValidationError(messageBody)) {
                    setBtnProgressEnabled(false)
                } else {
                    createAccount(parameters)
                }
            }

            override fun onFailure(error: Throwable) {
                setBtnProgressEnabled(false)
                this@RegisterActivity.showAlertDialog(
                    null, ErrorUtils.getErrorMessage(
                        error,
                        this@RegisterActivity
                    )
                )
                logger.error(error)
            }
        })
    }

    private fun hasValidationError(messageBody: HashMap<String, String>): Boolean {
        var errorShown = false
        for (key in messageBody.keys) {
            for (fieldView in mFieldViews) {
                val error = messageBody[key]
                if (error.isNotNullOrEmpty() &&
                    key.equals(fieldView.getField().name, ignoreCase = true)
                ) {
                    fieldView.handleError(error)
                    if (!errorShown) {
                        // this is the first input field with error,
                        // so focus on it after showing the popup
                        showErrorPopup(fieldView.getOnErrorFocusView())
                        errorShown = true
                    }
                    break
                }
            }
        }
        return errorShown
    }

    private fun showErrorMessage(errorMsg: String, @DrawableRes errorIconResId: Int) {
        errorNotification.showError(
            errorMsg,
            errorIconResId,
            R.string.lbl_reload
        ) { getRegistrationForm() }
    }

    private fun getRegistrationForm() {
        if (!NetworkUtil.isConnected(this)) {
            showErrorMessage(getString(R.string.reset_no_network_message), R.drawable.ic_wifi)
            return
        }
        tryToSetUIInteraction(false)
        binding.loadingIndicator.root.setVisibility(true)
        errorNotification.hideError()
        val getRegistrationFormCall = loginService.getRegistrationForm(
            environment.config.apiUrlVersionConfig.registrationApiVersion
        )
        getRegistrationFormCall.enqueue(object :
            ErrorHandlingCallback<RegistrationDescription>(this) {
            override fun onResponse(registrationDescription: RegistrationDescription) {
                updateUI(true)
                setupRegistrationForm(registrationDescription)
                setRegistrationFields(savedRegistrationFormState)
                if (socialRegistrationType != SocialAuthSource.UNKNOWN) {
                    updateUIOnSocialLoginToEdxFailure(socialRegistrationType)
                } else {
                    // In case the user attempted social registration in a previous instance
                    loginPrefs.clearSocialLoginToken()
                }
            }

            override fun onFailure(error: Throwable) {
                updateUI(false)
                showErrorMessage(
                    ErrorUtils.getErrorMessage(error, this@RegisterActivity),
                    R.drawable.ic_error
                )
                logger.error(error)
            }

            private fun updateUI(isSuccess: Boolean) {
                tryToSetUIInteraction(true)
                binding.scrollview.setVisibility(isSuccess)
                binding.loadingIndicator.root.setVisibility(false)
            }
        })
    }

    private fun setupRegistrationForm(form: RegistrationDescription) {
        val inflater = layoutInflater
        for (field in form.fields) {
            val fieldView = getInstance(inflater, field)
            if (fieldView != null) mFieldViews.add(fieldView)
        }

        // add required and optional fields to the window
        for (regField in mFieldViews) {
            if (regField.getField().isRequired) {
                binding.requiredFieldsLayout.addView(regField.getView())
            } else if (!regField.getField().isRequired && regField.getField().isExposed) {
                binding.optionallyExposedFieldsLayout.addView(regField.getView())
            } else {
                binding.optionalFieldsLayout.addView(regField.getView())
            }
        }
        binding.optionalFieldTv.setVisibility(binding.optionalFieldsLayout.childCount != 0)

        // enable all the views
        tryToSetUIInteraction(true)
    }

    private fun createAccount(parameters: Bundle) {
        authViewModel.registerAccount(parameters)

        val provider = environment.loginPrefs.socialLoginProvider
        val appVersion = "${getString(R.string.android)} ${BuildConfig.VERSION_NAME}"
        environment.analyticsRegistry.trackCreateAccountClicked(appVersion, provider)
    }

    // this is the first input field with error,
    // so focus on it after showing the popup
    // do NOT proceed if validations are failed
    // we submit the field only if it provides a value
    // Validating email field with confirm email field
    private fun getRegistrationParameters(): Bundle? {
        var hasError = false
        val parameters = Bundle()
        var email: String? = null
        var confirmEmail: String? = null
        for (fieldView in mFieldViews) {
            if (fieldView.isValidInput()) {
                if (fieldView.getField().isEmailField) {
                    email = fieldView.getCurrentValue()?.asString
                }
                if (fieldView.getField().isConfirmEmailField) {
                    confirmEmail = fieldView.getCurrentValue()?.asString
                }

                // Validating email field with confirm email field
                if (email.isNotNullOrEmpty() &&
                    confirmEmail.isNotNullOrEmpty() &&
                    !email.equals(confirmEmail)
                ) {
                    fieldView.handleError(fieldView.getField().errorMessage?.required)
                    showErrorPopup(fieldView.getOnErrorFocusView())
                    return null
                }
                if (fieldView.hasValue()) {
                    // we submit the field only if it provides a value
                    parameters.putString(
                        fieldView.getField().name,
                        fieldView.getCurrentValue()?.asString
                    )
                }
            } else {
                if (!hasError) {
                    // this is the first input field with error,
                    // so focus on it after showing the popup
                    showErrorPopup(fieldView.getOnErrorFocusView())
                }
                hasError = true
            }
        }
        // do NOT proceed if validations are failed
        return if (hasError) null else parameters
    }

    /**
     * Displays given errors on the given registration field.
     *
     * @param errors
     * @param fieldView
     * @return
     */
    private fun showErrorOnField(
        errors: List<RegisterResponseFieldError>,
        fieldView: IRegistrationFieldView
    ) {
        if (errors.isNotEmpty()) {
            val buffer = StringBuffer()
            for (e in errors) {
                buffer.append(e.userMessage + " ")
            }
            fieldView.handleError(buffer.toString())
        }
    }

    private fun showErrorPopup(errorView: View) {
        showAlertDialog(
            resources.getString(R.string.registration_error_title),
            resources.getString(R.string.registration_error_message)
        ) { _, _ ->
            scrollToView(binding.scrollview, errorView)
        }
    }

    /**
     * we can create enum for strong type, but lose the extensibility.
     *
     * @param socialAuthSource
     */
    private fun showRegularMessage(socialAuthSource: SocialAuthSource) {
        binding.messageLayout.title.text = when (socialAuthSource) {
            SocialAuthSource.FACEBOOK -> getString(R.string.sign_up_with_facebook_ok)
            SocialAuthSource.MICROSOFT -> getString(R.string.sign_up_with_microsoft_ok)
            SocialAuthSource.GOOGLE -> getString(R.string.sign_up_with_google_ok)
            else -> ""
        }
        binding.messageLayout.message.text = ResourceUtil.getFormattedString(
            resources,
            R.string.sign_up_with_social_ok,
            AppConstants.PLATFORM_NAME,
            environment.config.platformName
        )
        binding.messageLayout.root.setVisibility(true)
    }

    private fun updateUIOnSocialLoginToEdxFailure(
        socialAuthSource: SocialAuthSource,
        accessToken: String? = null
    ) {
        //change UI.
        binding.socialAuth.root.setVisibility(false)
        //help method
        showRegularMessage(socialAuthSource)
        //populate the field with value from social site
        accessToken?.let { populateEmailFromSocialSite(socialAuthSource, it) }
        //hide confirm email and password field as we don't need them in case of social signup
        val extraFields: MutableList<IRegistrationFieldView> = ArrayList()
        for (field in mFieldViews) {
            if (field.getField().isConfirmEmailField || field.getField().isPasswordField) {
                field.getView().setVisibility(false)
                extraFields.add(field)
            }
        }
        mFieldViews.removeAll(extraFields)
    }

    private fun populateFormField(fieldName: String, value: String?) {
        for (field in mFieldViews) {
            if (fieldName.equals(field.getField().name, ignoreCase = true)) {
                val success = field.setRawValue(value)
                if (success) break
            }
        }
    }

    private fun populateEmailFromSocialSite(
        socialAuthSource: SocialAuthSource,
        accessToken: String
    ) {
        socialLoginDelegate.getUserInfo(
            socialAuthSource,
            accessToken,
            object : SocialLoginDelegate.SocialUserInfoCallback {
                override fun setSocialUserInfo(email: String?, name: String?) {
                    populateFormField("email", email)
                    if (name.isNotNullOrEmpty()) {
                        populateFormField("name", name)

                        //Should we save the email here?
                        environment.loginPrefs.lastAuthenticatedEmail = email
                    }
                }
            })
    }

    ///////section related to social login ///////////////
    // there are some duplicated code from login activity, as the logic
    //between login and registration is different subtly

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Get the registration form fields from the current state. If the fields are not loaded
        // yet, use the saved registration form state as a fallback.
        val formFields = getRegistrationFields().takeIf {
            it.isEmpty.not()
        } ?: savedRegistrationFormState
        outState.putBundle(REGISTRATION_FORM_DATA, formFields)
        outState.putSerializable(SOCIAL_REGISTRATION_TYPE, socialRegistrationType)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedRegistrationFormState = savedInstanceState.getBundle(REGISTRATION_FORM_DATA)
        socialRegistrationType = savedInstanceState.serializableOrThrow(SOCIAL_REGISTRATION_TYPE)
    }

    private fun getRegistrationFields(): Bundle {
        val parameters = Bundle()
        for (fieldView in mFieldViews) {
            if (fieldView.hasValue()) {
                parameters.putString(
                    fieldView.getField().name,
                    fieldView.getCurrentValue()?.asString
                )
            }
            if (fieldView.getView().hasFocus()) {
                parameters.putString(REGISTRATION_FOCUS_FIELD, fieldView.getField().name)
            }
        }
        return parameters
    }

    private fun setRegistrationFields(parameters: Bundle?) {
        parameters?.let {
            val focusedFieldName = parameters.getString(REGISTRATION_FOCUS_FIELD, null)
            for (fieldView in mFieldViews) {
                fieldView.setRawValue(parameters.getString(fieldView.getField().name))
                if (fieldView.getField().name.equals(focusedFieldName))
                    fieldView.getView().requestFocus()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data)
        tryToSetUIInteraction(true)
    }

    /*
     *  callback if login to edx success using social access_token
     */
    private fun onUserLoginSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    override fun performUserLogin(accessToken: String, backend: String, feature: Feature) {
        authViewModel.loginUsingSocialAccount(accessToken, backend, feature)
    }

    /**
     * Show/Hide loading progress on Create Account button
     *
     * @param enable flag to enable/disable view
     */
    private fun setBtnProgressEnabled(enable: Boolean) {
        tryToSetUIInteraction(enable.not())
        binding.btnProgress.progressIndicator.setVisibility(enable)
        binding.createAccountTv.text = getString(
            if (enable) {
                R.string.creating_account_text
            } else {
                R.string.create_account_text
            }
        )
    }

    /**
     * Enable/Disable the Create button during server calls
     *
     * @param enable flag to enable/disable view
     */
    private fun setCreateAccBtnEnabled(enable: Boolean) {
        binding.createAccountBtn.isEnabled = enable
        binding.createAccountTv.text = getString(R.string.create_account_text)
    }

    override fun tryToSetUIInteraction(enable: Boolean): Boolean {
        setTouchEnabled(enable)
        setCreateAccBtnEnabled(enable)
        for (v in mFieldViews) {
            v.setEnabled(enable)
            setActionListeners(v)
        }
        binding.socialAuth.facebookButton.isClickable = enable
        binding.socialAuth.googleButton.isClickable = enable
        binding.socialAuth.microsoftButton.isClickable = enable
        return true
    }

    /**
     * Sets actions listener on views used in Registration form
     *
     * @param view
     */
    private fun setActionListeners(view: IRegistrationFieldView) {
        if (RegistrationFieldType.CHECKBOX == view.getField().fieldType) {
            view.setActionListener(object : IActionListener {
                override fun onClickAgreement() {
                    if (view.getCurrentValue()?.asBoolean == true) {
                        environment.analyticsRegistry.trackEvent(
                            Analytics.Events.REGISTRATION_OPT_IN_TURNED_ON,
                            Analytics.Values.REGISTRATION_OPT_IN_TURNED_ON
                        )
                    } else {
                        environment.analyticsRegistry.trackEvent(
                            Analytics.Events.REGISTRATION_OPT_IN_TURNED_OFF,
                            Analytics.Values.REGISTRATION_OPT_IN_TURNED_OFF
                        )
                    }
                }
            })
        }
    }

    /**
     * Scrolls to the top of the given View in the given ScrollView.
     *
     * @param scrollView
     * @param view
     */
    private fun scrollToView(scrollView: ScrollView, view: View) {
        /*
    The delayed focus has been added so that TalkBack reads the proper view's description that
    we want focus on. For example in case of {@link RegistrationEditText} we want accessibility
    focus on TIL when an error is displayed instead of the EditText within it, which can only
    be achieved through this delay.
     */
        view.postDelayed({
            view.requestFocus()
            view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }, ACCESSIBILITY_FOCUS_DELAY_MS.toLong())

        // Determine if scroll needs to happen
        val scrollBounds = Rect()
        scrollView.getHitRect(scrollBounds)
        if (!view.getLocalVisibleRect(scrollBounds)) {
            Runnable { scrollView.smoothScrollTo(0, view.top) }
        }
    }

    companion object {
        private const val REGISTRATION_FORM_DATA = "registration_form_data"
        private const val SOCIAL_REGISTRATION_TYPE = "social_registration_type"
        private const val REGISTRATION_FOCUS_FIELD = "registration_focus_field"
        private const val ACCESSIBILITY_FOCUS_DELAY_MS = 500

        @JvmStatic
        fun newIntent(): Intent {
            return IntentFactory.newIntentForComponent(RegisterActivity::class.java)
        }
    }
}
