package org.edx.mobile.view.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.BuildConfig
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginTask
import org.edx.mobile.databinding.ActivityLoginBinding
import org.edx.mobile.deeplink.DeepLink
import org.edx.mobile.deeplink.DeepLinkManager
import org.edx.mobile.exception.LoginErrorMessage
import org.edx.mobile.exception.LoginException
import org.edx.mobile.extenstion.isNotNullOrEmpty
import org.edx.mobile.extenstion.parcelable
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.social.SocialFactory
import org.edx.mobile.social.SocialLoginDelegate
import org.edx.mobile.social.SocialLoginDelegate.MobileLoginCallback
import org.edx.mobile.task.Task
import org.edx.mobile.util.AppStoreUtils
import org.edx.mobile.util.IntentFactory
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.TextUtils
import org.edx.mobile.util.images.ErrorUtils
import org.edx.mobile.view.PresenterActivity
import org.edx.mobile.view.Router
import org.edx.mobile.view.dialog.ResetPasswordDialogFragment
import org.edx.mobile.view.login.LoginPresenter.LoginViewInterface

@AndroidEntryPoint
class LoginActivity : PresenterActivity<LoginPresenter, LoginViewInterface>(),
    MobileLoginCallback {
    private lateinit var socialLoginDelegate: SocialLoginDelegate
    private lateinit var binding: ActivityLoginBinding

    val email: String
        get() = binding.emailEt.text.toString().trim()

    val password: String
        get() = binding.passwordEt.text.toString().trim()

    val loginException: LoginException
        get() = LoginException(
            LoginErrorMessage(
                getString(R.string.login_error),
                getString(R.string.login_failed)
            )
        )

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun createPresenter(savedInstanceState: Bundle?): LoginPresenter {
        return LoginPresenter(environment.config)
    }

    override fun createView(savedInstanceState: Bundle?): LoginViewInterface {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews(savedInstanceState)
        hideSoftKeypad()
        // enable login buttons at launch
        tryToSetUIInteraction(true)
        environment.analyticsRegistry.trackScreenView(Analytics.Screens.LOGIN)

        return object : LoginViewInterface {
            override fun disableToolbarNavigation() {
                supportActionBar?.apply {
                    setHomeButtonEnabled(false)
                    setDisplayHomeAsUpEnabled(false)
                    setDisplayShowHomeEnabled(false)
                }
            }

            override fun setSocialLoginButtons(
                googleEnabled: Boolean, facebookEnabled: Boolean,
                microsoftEnabled: Boolean
            ) {
                binding.socialAuth.apply {
                    root.setVisibility((!facebookEnabled && !googleEnabled && !microsoftEnabled).not())
                    googleButton.setVisibility(googleEnabled)
                    facebookButton.setVisibility(facebookEnabled)
                    microsoftButton.setVisibility(microsoftEnabled)
                }
            }
        }
    }

    private fun initViews(savedInstanceState: Bundle?) {
        title = getString(R.string.login_title)

        binding.loginButtonLayout.setOnClickListener {
            callServerForLogin()
        }
        binding.forgotPasswordTv.setOnClickListener { // Calling help dialog
            if (NetworkUtil.isConnected(this@LoginActivity)) {
                showResetPasswordDialog()
            } else {
                showAlertDialog(
                    getString(R.string.reset_no_network_title),
                    getString(R.string.network_not_connected)
                )
            }
        }
        binding.emailEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun onTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun afterTextChanged(editable: Editable) {
                binding.usernameWrapper.error = null
            }
        })
        binding.passwordEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun onTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun afterTextChanged(editable: Editable) {
                binding.passwordWrapper.error = null
            }
        })
        setupSocialLogin(savedInstanceState)
        initEULA()
    }

    private fun setupSocialLogin(savedInstanceState: Bundle?) {
        socialLoginDelegate = SocialLoginDelegate(
            this, savedInstanceState, this,
            environment.config, environment.loginPrefs, SocialLoginDelegate.Feature.SIGN_IN
        ).apply {
            binding.socialAuth.facebookButton.setOnClickListener {
                createSocialButtonClickHandler(SocialFactory.SocialSourceType.FACEBOOK)
            }
            binding.socialAuth.googleButton.setOnClickListener(
                createSocialButtonClickHandler(SocialFactory.SocialSourceType.GOOGLE)
            )
            binding.socialAuth.microsoftButton.setOnClickListener(
                createSocialButtonClickHandler(SocialFactory.SocialSourceType.MICROSOFT)
            )
        }
    }

    private fun initEULA() {
        binding.endUserAgreementTv.movementMethod = LinkMovementMethod.getInstance()
        binding.endUserAgreementTv.text = TextUtils.generateLicenseText(
            environment.config, this, R.string.by_signing_in
        )

        val envDisplayName = environment.config.environmentDisplayName
        if (envDisplayName.isNotNullOrEmpty()) {
            binding.versionEnvTv.setVisibility(true)
            val versionName = BuildConfig.VERSION_NAME
            val text = String.format(
                "%s %s %s",
                getString(R.string.label_version),
                versionName,
                envDisplayName
            )
            binding.versionEnvTv.text = text
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socialLoginDelegate.onActivityDestroyed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("username", email)
        socialLoginDelegate.onActivitySaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (email.isEmpty()) {
            displayLastEmailId()
        }
        socialLoginDelegate.onActivityStarted()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.emailEt.setText(savedInstanceState.getString("username"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tryToSetUIInteraction(true)
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ResetPasswordDialogFragment.REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    showAlertDialog(
                        getString(R.string.success_dialog_title_help),
                        getString(R.string.success_dialog_message_help)
                    )
                }
            }
        }
    }

    private fun displayLastEmailId() {
        binding.emailEt.setText(environment.loginPrefs.lastAuthenticatedEmail)
    }

    @SuppressLint("StaticFieldLeak")
    fun callServerForLogin() {
        if (!NetworkUtil.isConnected(this)) {
            showAlertDialog(
                getString(R.string.no_connectivity),
                getString(R.string.network_not_connected)
            )
            return
        }
        if (password.isEmpty()) {
            binding.passwordWrapper.error = getString(R.string.error_enter_password)
            binding.passwordEt.requestFocus()
        }
        if (email.isEmpty()) {
            binding.usernameWrapper.error = getString(R.string.error_enter_email)
            binding.emailEt.requestFocus()
        }
        if (email.isNotEmpty() && password.isNotEmpty()) {
            binding.emailEt.isEnabled = false
            binding.passwordEt.isEnabled = false
            binding.forgotPasswordTv.isEnabled = false
            binding.endUserAgreementTv.isEnabled = false

            val loginTask: LoginTask = object : LoginTask(this, email, password) {
                override fun onPostExecute(result: AuthResponse?) {
                    super.onPostExecute(result)
                    if (result != null) {
                        onUserLoginSuccess()
                    }
                }

                override fun onException(ex: Exception) {
                    if (ex is HttpStatusException && ex.statusCode == HttpStatus.BAD_REQUEST) {
                        onUserLoginFailure(loginException, null, null)
                    } else {
                        onUserLoginFailure(ex, null, null)
                    }
                }
            }
            tryToSetUIInteraction(false)
            loginTask.setProgressDialog(binding.progress.progressIndicator)
            loginTask.execute()
        }
    }

    override fun onStop() {
        super.onStop()
        socialLoginDelegate.onActivityStopped()
    }

    private fun showResetPasswordDialog() {
        ResetPasswordDialogFragment.newInstance(email).show(supportFragmentManager, null)
    }

    // make sure that on the login activity, all errors show up as a dialog as opposed to a flying snackbar
    override fun showAlertDialog(header: String?, message: String) {
        super.showAlertDialog(header, message)
    }

    /**
     * Starts fetching profile of the user after login by Facebook or Google.
     *
     * @param accessToken
     * @param backend
     */
    override fun onSocialLoginSuccess(accessToken: String, backend: String, task: Task<*>) {
        tryToSetUIInteraction(false)
        task.setProgressDialog(binding.progress.progressIndicator)
    }

    override fun onUserLoginSuccess() {
        setResult(RESULT_OK)
        finish()
        val deepLink = intent.parcelable<DeepLink>(Router.EXTRA_DEEP_LINK)
        if (deepLink != null) {
            DeepLinkManager.onDeepLinkReceived(this, deepLink)
            return
        }
        if (!environment.config.isRegistrationEnabled) {
            environment.router.showMainDashboard(this)
        }
    }

    override fun onUserLoginFailure(ex: Exception, accessToken: String?, backend: String?) {
        tryToSetUIInteraction(true)
        when (ex) {
            is LoginException -> {
                val errorMessage = ex.loginErrorMessage
                showAlertDialog(errorMessage.messageLine1, errorMessage.messageLine2)
            }

            is HttpStatusException -> {
                when (ex.statusCode) {
                    HttpStatus.UPGRADE_REQUIRED -> this@LoginActivity.showAlertDialog(
                        null,
                        getString(R.string.app_version_unsupported_login_msg),
                        getString(R.string.label_update),
                        { _, _ ->
                            AppStoreUtils
                                .openAppInAppStore(this@LoginActivity)
                        },
                        getString(android.R.string.cancel), null
                    )

                    HttpStatus.FORBIDDEN -> this@LoginActivity.showAlertDialog(
                        getString(R.string.login_error),
                        getString(R.string.auth_provider_disabled_user_error),
                        getString(R.string.label_customer_support),
                        { _, _ ->
                            environment.router
                                .showFeedbackScreen(
                                    this@LoginActivity,
                                    getString(R.string.email_subject_account_disabled)
                                )
                        }, getString(android.R.string.cancel), null
                    )

                    else -> {
                        showAlertDialog(
                            getString(R.string.login_error),
                            ErrorUtils.getErrorMessage(ex, this@LoginActivity)
                        )
                        logger.error(ex)
                    }
                }
            }

            else -> {
                showAlertDialog(
                    getString(R.string.login_error),
                    ErrorUtils.getErrorMessage(ex, this@LoginActivity)
                )
                logger.error(ex)
            }
        }
    }

    override fun tryToSetUIInteraction(enable: Boolean): Boolean {
        setTouchEnabled(enable)
        binding.apply {
            loginButtonLayout.isEnabled = enable
            emailEt.isEnabled = enable
            passwordEt.isEnabled = enable
            loginBtnTv.text = getString(if (enable) R.string.login_title else R.string.signing_in)
            forgotPasswordTv.isEnabled = enable
            socialAuth.facebookButton.isClickable = enable
            socialAuth.googleButton.isClickable = enable
            socialAuth.microsoftButton.isClickable = enable
            endUserAgreementTv.isEnabled = enable
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newIntent(deepLink: DeepLink?): Intent {
            val intent = IntentFactory.newIntentForComponent(LoginActivity::class.java)
            intent.putExtra(Router.EXTRA_DEEP_LINK, deepLink)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            return intent
        }
    }
}
