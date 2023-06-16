package org.edx.mobile.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.ScrollView
import androidx.annotation.DrawableRes
import androidx.databinding.DataBindingUtil
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
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.callback.ErrorHandlingCallback
import org.edx.mobile.http.constants.ApiConstants
import org.edx.mobile.model.api.RegisterResponseFieldError
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.module.registration.model.RegistrationDescription
import org.edx.mobile.module.registration.model.RegistrationFieldType
import org.edx.mobile.module.registration.view.IRegistrationFieldView
import org.edx.mobile.module.registration.view.IRegistrationFieldView.Factory.getInstance
import org.edx.mobile.module.registration.view.IRegistrationFieldView.IActionListener
import org.edx.mobile.social.SocialFactory
import org.edx.mobile.social.SocialFactory.SOCIAL_SOURCE_TYPE
import org.edx.mobile.social.SocialLoginDelegate
import org.edx.mobile.social.SocialLoginDelegate.MobileLoginCallback
import org.edx.mobile.task.RegisterTask
import org.edx.mobile.task.Task
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.AppStoreUtils
import org.edx.mobile.util.IntentFactory
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.util.TextUtils
import org.edx.mobile.util.UiUtils.getDrawable
import org.edx.mobile.util.images.ErrorUtils
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : BaseFragmentActivity(), MobileLoginCallback {

    private lateinit var mBinding: ActivityRegisterBinding
    private val mFieldViews: MutableList<IRegistrationFieldView> = ArrayList()
    private var socialLoginDelegate: SocialLoginDelegate? = null

    @Inject
    lateinit var loginPrefs: LoginPrefs

    @Inject
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        super.setToolbarAsActionBar()
        setTitle(R.string.register_title)
        environment.analyticsRegistry.trackScreenView(Analytics.Screens.REGISTER)
        getRegistrationForm()
        hideSoftKeypad()
        tryToSetUIInteraction(true)
        initEULA()
        mBinding.createAccountBtn.setOnClickListener {
            hideSoftKeypad()
            validateRegistrationFields()
        }
        mBinding.optionalFieldTv.setOnCheckedChangeListener { _, isChecked: Boolean ->
            mBinding.optionalFieldsLayout.setVisibility(
                isChecked
            )
        }
        setupSocialAuth(savedInstanceState)
    }

    private fun initEULA() {
        mBinding.eulaTv.movementMethod = LinkMovementMethod.getInstance()
        mBinding.eulaTv.text = TextUtils.generateLicenseText(
            environment.config, this, R.string.by_creating_account
        )
    }

    private fun setupSocialAuth(savedInstanceState: Bundle?) {
        socialLoginDelegate = SocialLoginDelegate(
            this, savedInstanceState,
            this, environment.config, loginPrefs, SocialLoginDelegate.Feature.REGISTRATION
        )
        var isSocialEnabled = false
        if (isSocialFeatureEnable(SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK)) {
            isSocialEnabled = true
            mBinding.socialAuth.facebookButton.text =
                getString(R.string.continue_with_social, getString(R.string.facebook_text))
            mBinding.socialAuth.facebookButton.setOnClickListener(
                socialLoginDelegate?.createSocialButtonClickHandler(
                    SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK
                )
            )
        } else {
            mBinding.socialAuth.facebookButton.setVisibility(false)
        }
        if (isSocialFeatureEnable(SOCIAL_SOURCE_TYPE.TYPE_GOOGLE)) {
            isSocialEnabled = true
            mBinding.socialAuth.googleButton.text =
                getString(R.string.continue_with_social, getString(R.string.google_text))
            mBinding.socialAuth.googleButton.setOnClickListener(
                socialLoginDelegate?.createSocialButtonClickHandler(
                    SOCIAL_SOURCE_TYPE.TYPE_GOOGLE
                )
            )
        } else {
            mBinding.socialAuth.googleButton.setVisibility(false)
        }
        if (isSocialFeatureEnable(SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT)) {
            isSocialEnabled = true
            mBinding.socialAuth.microsoftButton.text =
                getString(R.string.continue_with_social, getString(R.string.microsoft_text))
            mBinding.socialAuth.microsoftButton.setOnClickListener(
                socialLoginDelegate?.createSocialButtonClickHandler(SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT)
            )
        } else {
            mBinding.socialAuth.microsoftButton.setVisibility(false)
        }
        mBinding.socialAuth.root.setVisibility(isSocialEnabled)
    }

    private fun isSocialFeatureEnable(type: SOCIAL_SOURCE_TYPE): Boolean {
        return SocialFactory.isSocialFeatureEnabled(application, type, environment.config)
    }

    private fun validateRegistrationFields() {
        val parameters = getRegistrationParameters() ?: return

        invalidateBtnProgress(true)
        val parameterMap: MutableMap<String, String?> = HashMap()
        for (key in parameters.keySet()) {
            parameterMap[key] = parameters.getString(key)
        }
        val validateRegistrationFields = loginService.validateRegistrationFields(parameterMap)
        validateRegistrationFields.enqueue(object : ErrorHandlingCallback<JsonObject?>(this) {
            override fun onResponse(responseBody: JsonObject) {
                // Callback method for a successful HTTP response.
                val stringMapType = object : TypeToken<HashMap<String?, String?>?>() {}.type
                val messageBody = Gson().fromJson<HashMap<String, String>>(
                    responseBody[ApiConstants.VALIDATION_DECISIONS], stringMapType
                )
                if (hasValidationError(messageBody)) {
                    invalidateBtnProgress(false)
                } else {
                    createAccount(parameters)
                }
            }

            override fun onFailure(error: Throwable) {
                invalidateBtnProgress(false)
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
        mBinding.errorMessage.visibility = View.VISIBLE
        mBinding.errorMessage.text = errorMsg
        mBinding.errorMessage.setCompoundDrawables(
            null, getDrawable(
                this, errorIconResId,
                R.dimen.content_unavailable_error_icon_size, R.color.neutralDark
            ),
            null, null
        )
    }

    private fun getRegistrationForm() {
        if (!NetworkUtil.isConnected(this)) {
            showErrorMessage(getString(R.string.reset_no_network_message), R.drawable.ic_wifi)
            return
        }
        tryToSetUIInteraction(false)
        mBinding.loadingIndicator.root.visibility = View.VISIBLE
        val getRegistrationFormCall = loginService.getRegistrationForm(
            environment.config.apiUrlVersionConfig.registrationApiVersion
        )
        getRegistrationFormCall.enqueue(object :
            ErrorHandlingCallback<RegistrationDescription?>(this) {
            override fun onResponse(registrationDescription: RegistrationDescription) {
                updateUI(true)
                setupRegistrationForm(registrationDescription)
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
                mBinding.registrationForm.setVisibility(isSuccess)
                mBinding.loadingIndicator.root.visibility = View.GONE
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
        for (v in mFieldViews) {
            if (v.getField().isRequired) {
                mBinding.requiredFieldsLayout.addView(v.getView())
            } else if (!v.getField().isRequired && v.getField().isExposed) {
                mBinding.optionallyExposedFieldsLayout.addView(v.getView())
            } else {
                mBinding.optionalFieldsLayout.addView(v.getView())
            }
        }
        if (mBinding.optionalFieldsLayout.childCount == 0) mBinding.optionalFieldTv.visibility =
            View.GONE

        // enable all the views
        tryToSetUIInteraction(true)
    }

    private fun createAccount(parameters: Bundle) {
        // set honor_code and terms_of_service to true
        parameters.putString("honor_code", "true")
        parameters.putString("terms_of_service", "true")

        //set parameter required by social registration
        val accessToken = loginPrefs.socialLoginAccessToken
        val provider = loginPrefs.socialLoginProvider
        if (accessToken.isNotNullOrEmpty()) {
            parameters.putString("access_token", accessToken)
            parameters.putString("provider", provider)
            parameters.putString("client_id", environment.config.oAuthClientId)
        }

        // Send analytics event for Create Account button click
        val appVersion =
            String.format("%s v%s", getString(R.string.android), BuildConfig.VERSION_NAME)
        environment.analyticsRegistry.trackCreateAccountClicked(appVersion, provider)
        val backSourceType = SOCIAL_SOURCE_TYPE.fromString(provider)
        @SuppressLint("StaticFieldLeak") val task: RegisterTask =
            object : RegisterTask(this, parameters, accessToken, backSourceType) {
                @Deprecated("Deprecated in Java")
                override fun onPostExecute(auth: AuthResponse?) {
                    super.onPostExecute(auth)
                    if (auth != null) {
                        environment.analyticsRegistry.trackRegistrationSuccess(appVersion, provider)
                        onUserLoginSuccess()
                    }
                }

                override fun onException(ex: Exception) {
                    invalidateBtnProgress(false)
                    if (ex is RegistrationException) {
                        val messageBody = ex.formErrorBody
                        var errorShown = false
                        for (key in messageBody.keys) {
                            if (key == null) continue
                            for (fieldView in mFieldViews) {
                                if (key.equals(fieldView.getField().name, ignoreCase = true)) {
                                    val error = messageBody[key]
                                    showErrorOnField(error, fieldView)
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
                        if (errorShown) {
                            // We have already shown a specific error message.
                            return  // Return here to avoid falling back to the generic error handler.
                        }
                    }
                    // If app version is un-supported
                    if (ex is HttpStatusException &&
                        ex.statusCode == HttpStatus.UPGRADE_REQUIRED
                    ) {
                        this@RegisterActivity.showAlertDialog(
                            null,
                            getString(R.string.app_version_unsupported_register_msg),
                            getString(R.string.label_update),
                            { _, _ -> AppStoreUtils.openAppInAppStore(this@RegisterActivity) },
                            getString(android.R.string.cancel),
                            null
                        )
                    } else {
                        this@RegisterActivity.showAlertDialog(
                            null,
                            ErrorUtils.getErrorMessage(ex, this@RegisterActivity)
                        )
                    }
                }
            }
        task.execute()
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
        for (v in mFieldViews) {
            if (v.isValidInput()) {
                if (v.getField().isEmailField) {
                    email = v.getCurrentValue()?.asString
                }
                if (v.getField().isConfirmEmailField) {
                    confirmEmail = v.getCurrentValue()?.asString
                }

                // Validating email field with confirm email field
                if (email.isNotNullOrEmpty() &&
                    confirmEmail.isNotNullOrEmpty() &&
                    !email.equals(confirmEmail)
                ) {
                    v.handleError(v.getField().errorMessage?.required)
                    showErrorPopup(v.getOnErrorFocusView())
                    return null
                }
                if (v.hasValue()) {
                    // we submit the field only if it provides a value
                    parameters.putString(v.getField().name, v.getCurrentValue()?.asString)
                }
            } else {
                if (!hasError) {
                    // this is the first input field with error,
                    // so focus on it after showing the popup
                    showErrorPopup(v.getOnErrorFocusView())
                }
                hasError = true
            }
        }
        // do NOT proceed if validations are failed
        return if (hasError) {
            null
        } else parameters
    }

    /**
     * Displays given errors on the given registration field.
     *
     * @param errors
     * @param fieldView
     * @return
     */
    private fun showErrorOnField(
        errors: List<RegisterResponseFieldError>?,
        fieldView: IRegistrationFieldView
    ) {
        if (!errors.isNullOrEmpty()) {
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
            scrollToView(mBinding.scrollview, errorView)
        }
    }

    /**
     * we can create enum for strong type, but lose the extensibility.
     *
     * @param socialType
     */
    private fun showRegularMessage(socialType: SOCIAL_SOURCE_TYPE) {
        mBinding.messageLayout.title.text = when (socialType) {
            SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK ->
                getString(R.string.sign_up_with_facebook_ok)

            SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT ->
                getString(R.string.sign_up_with_microsoft_ok)

            SOCIAL_SOURCE_TYPE.TYPE_GOOGLE ->
                getString(R.string.sign_up_with_google_ok)

            else -> ""
        }
        mBinding.messageLayout.message.text = ResourceUtil.getFormattedString(
            resources,
            R.string.sign_up_with_social_ok,
            AppConstants.PLATFORM_NAME,
            environment.config.platformName
        )
        mBinding.messageLayout.root.visibility = View.VISIBLE
    }

    private fun updateUIOnSocialLoginToEdxFailure(
        socialType: SOCIAL_SOURCE_TYPE,
        accessToken: String
    ) {
        //change UI.
        val socialPanel = findViewById<View>(R.id.panel_social_layout)
        socialPanel.visibility = View.GONE
        //help method
        showRegularMessage(socialType)
        //populate the field with value from social site
        populateEmailFromSocialSite(socialType, accessToken)
        //hide confirm email and password field as we don't need them in case of social signup
        val extraFields: MutableList<IRegistrationFieldView> = ArrayList()
        for (field in mFieldViews) {
            if (field.getField().isConfirmEmailField || field.getField().isPasswordField) {
                field.getView().setVisibility(false)
                extraFields.add(field)
            }
        }
        mFieldViews.removeAll(extraFields)
        // registrationLayout.requestLayout();
    }

    private fun populateFormField(fieldName: String, value: String?) {
        for (field in mFieldViews) {
            if (fieldName.equals(field.getField().name, ignoreCase = true)) {
                val success = field.setRawValue(value)
                if (success) break
            }
        }
    }

    private fun populateEmailFromSocialSite(socialType: SOCIAL_SOURCE_TYPE, accessToken: String) {
        socialLoginDelegate?.getUserInfo(socialType, accessToken) { email, name ->
            populateFormField("email", email)
            if (name.isNotNullOrEmpty()) {
                populateFormField("name", name)

                //Should we save the email here?
                loginPrefs.lastAuthenticatedEmail = email
            }
        }
    }

    ///////section related to social login ///////////////
    // there are some duplicated code from login activity, as the logic
    //between login and registration is different subtly
    override fun onDestroy() {
        super.onDestroy()
        socialLoginDelegate?.onActivityDestroyed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // outState.putString("username", email_et.getText().toString().trim());
        socialLoginDelegate?.onActivitySaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        socialLoginDelegate?.onActivityStopped()
    }

    override fun onStart() {
        super.onStart()
        socialLoginDelegate?.onActivityStarted()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialLoginDelegate?.onActivityResult(requestCode, resultCode, data)
        tryToSetUIInteraction(true)
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
    override fun onSocialLoginSuccess(accessToken: String, backend: String, task: Task<*>?) {
        //we should handle UI update here. but right now we do nothing in UI
    }

    /*
     *  callback if login to edx success using social access_token
     */
    override fun onUserLoginSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    /**
     * callback if login to edx failed using social access_token
     */
    override fun onUserLoginFailure(ex: Exception, accessToken: String, backend: String) {
        // FIXME: We are assuming that if we get here, the accessToken is valid. That may not be the case!

        //we should redirect to current page.
        //do nothing
        //we need to add 1)access_token   2) provider 3) client_id
        // handle if this is a LoginException
        tryToSetUIInteraction(true)
        logger.error(ex)
        if (ex is HttpStatusException && ex.statusCode == HttpStatus.FORBIDDEN) {
            this@RegisterActivity.showAlertDialog(
                getString(R.string.login_error),
                getString(R.string.auth_provider_disabled_user_error),
                getString(R.string.label_customer_support),
                { _, _ ->
                    environment.router
                        .showFeedbackScreen(
                            this@RegisterActivity,
                            getString(R.string.email_subject_account_disabled)
                        )
                }, getString(android.R.string.cancel), null
            )
        } else {
            val socialType = SOCIAL_SOURCE_TYPE.fromString(backend)
            updateUIOnSocialLoginToEdxFailure(socialType, accessToken)
        }
    }

    /**
     * Show/Hide loading progress on Create Account button
     *
     * @param isEnable flag to enable/disable view
     */
    private fun invalidateBtnProgress(isEnable: Boolean) {
        tryToSetUIInteraction(isEnable)
        mBinding.btnProgress.progressIndicator.setVisibility(isEnable)
        mBinding.createAccountTv.text =
            getString(if (isEnable) R.string.creating_account_text else R.string.create_account_text)
    }

    /**
     * Enable/Disable the Create button during server calls
     *
     * @param isEnable flag to enable/disable view
     */
    private fun invalidateCreateAccBtn(isEnable: Boolean) {
        mBinding.createAccountBtn.isEnabled = isEnable
        mBinding.createAccountTv.text = getString(R.string.create_account_text)
    }

    override fun tryToSetUIInteraction(enable: Boolean): Boolean {
        invalidateTouch(enable)
        invalidateCreateAccBtn(enable)
        for (v in mFieldViews) {
            v.setEnabled(enable)
            setActionListeners(v)
        }
        mBinding.socialAuth.facebookButton.isClickable = enable
        mBinding.socialAuth.googleButton.isClickable = enable
        mBinding.socialAuth.microsoftButton.isClickable = enable
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
        private const val ACCESSIBILITY_FOCUS_DELAY_MS = 500

        @JvmStatic
        fun newIntent(): Intent {
            return IntentFactory.newIntentForComponent(RegisterActivity::class.java)
        }
    }
}
