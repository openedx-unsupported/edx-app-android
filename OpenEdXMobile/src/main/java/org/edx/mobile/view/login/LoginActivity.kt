package org.edx.mobile.view.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.BuildConfig
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginTask
import org.edx.mobile.databinding.ActivityLoginBinding
import org.edx.mobile.deeplink.DeepLink
import org.edx.mobile.deeplink.DeepLinkManager
import org.edx.mobile.exception.LoginErrorMessage
import org.edx.mobile.exception.LoginException
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.social.SocialFactory
import org.edx.mobile.social.SocialLoginDelegate
import org.edx.mobile.social.SocialLoginDelegate.MobileLoginCallback
import org.edx.mobile.task.Task
import org.edx.mobile.util.AppStoreUtils
import org.edx.mobile.util.IntentFactory
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NetworkUtil.ZeroRatedNetworkInfo
import org.edx.mobile.util.TextUtils
import org.edx.mobile.util.images.ErrorUtils
import org.edx.mobile.view.PresenterActivity
import org.edx.mobile.view.Router
import org.edx.mobile.view.dialog.ResetPasswordDialogFragment
import org.edx.mobile.view.login.LoginPresenter.LoginViewInterface
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : PresenterActivity<LoginPresenter, LoginViewInterface>(),
    MobileLoginCallback {
    private var socialLoginDelegate: SocialLoginDelegate? = null
    private lateinit var activityLoginBinding: ActivityLoginBinding

    @Inject
    lateinit var loginPrefs: LoginPrefs

    val email: String
        get() = activityLoginBinding.emailEt.text.toString().trim()

    val password: String
        get() = activityLoginBinding.passwordEt.text.toString().trim()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun createPresenter(savedInstanceState: Bundle?): LoginPresenter {
        return LoginPresenter(
            environment.config,
            ZeroRatedNetworkInfo(applicationContext, environment.config)
        )
    }

    override fun createView(savedInstanceState: Bundle?): LoginViewInterface {
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        hideSoftKeypad()
        setupSocialLogin(savedInstanceState)
        environment.analyticsRegistry.trackScreenView(Analytics.Screens.LOGIN)
        initViews()
        initEULA()
        // enable login buttons at launch
        tryToSetUIInteraction(true)

        return object : LoginViewInterface {
            override fun disableToolbarNavigation() {
                val actionBar = supportActionBar
                if (actionBar != null) {
                    actionBar.setHomeButtonEnabled(false)
                    actionBar.setDisplayHomeAsUpEnabled(false)
                    actionBar.setDisplayShowHomeEnabled(false)
                }
            }

            override fun setSocialLoginButtons(
                googleEnabled: Boolean, facebookEnabled: Boolean,
                microsoftEnabled: Boolean
            ) {
                activityLoginBinding.socialAuth.root.setVisibility((!facebookEnabled && !googleEnabled && !microsoftEnabled).not())
                activityLoginBinding.socialAuth.googleButton.setVisibility(googleEnabled)
                activityLoginBinding.socialAuth.facebookButton.setVisibility(facebookEnabled)
                activityLoginBinding.socialAuth.microsoftButton.setVisibility(microsoftEnabled)
            }
        }
    }

    private fun initViews() {
        title = getString(R.string.login_title)

        activityLoginBinding.loginButtonLayout.setOnClickListener { callServerForLogin() }
        activityLoginBinding.forgotPasswordTv.setOnClickListener { // Calling help dialog
            if (NetworkUtil.isConnected(this@LoginActivity)) {
                showResetPasswordDialog()
            } else {
                showAlertDialog(
                    getString(R.string.reset_no_network_title),
                    getString(R.string.network_not_connected)
                )
            }
        }
        activityLoginBinding.emailEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun onTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun afterTextChanged(editable: Editable) {
                activityLoginBinding.usernameWrapper.error = null
            }
        })
        activityLoginBinding.passwordEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun onTextChanged(c: CharSequence, s: Int, b: Int, a: Int) {}

            override fun afterTextChanged(editable: Editable) {
                activityLoginBinding.passwordWrapper.error = null
            }
        })
    }

    private fun initEULA() {
        activityLoginBinding.endUserAgreementTv.movementMethod = LinkMovementMethod.getInstance()
        activityLoginBinding.endUserAgreementTv.text = TextUtils.generateLicenseText(
            environment.config, this, R.string.by_signing_in
        )

        val envDisplayName = environment.config.environmentDisplayName
        if (envDisplayName.isNullOrEmpty().not()) {
            activityLoginBinding.versionEnvTv.setVisibility(true)
            val versionName = BuildConfig.VERSION_NAME
            val text = String.format(
                "%s %s %s",
                getString(R.string.label_version),
                versionName,
                envDisplayName
            )
            activityLoginBinding.versionEnvTv.text = text
        }
    }

    private fun setupSocialLogin(savedInstanceState: Bundle?) {
        socialLoginDelegate = SocialLoginDelegate(
            this, savedInstanceState, this,
            environment.config, environment.loginPrefs, SocialLoginDelegate.Feature.SIGN_IN
        )

        activityLoginBinding.socialAuth.facebookButton.setOnClickListener(
            socialLoginDelegate?.createSocialButtonClickHandler(
                SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK
            )
        )
        activityLoginBinding.socialAuth.googleButton.setOnClickListener(
            socialLoginDelegate?.createSocialButtonClickHandler(
                SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE
            )
        )
        activityLoginBinding.socialAuth.microsoftButton.setOnClickListener(
            socialLoginDelegate?.createSocialButtonClickHandler(
                SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        socialLoginDelegate?.onActivityDestroyed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("username", email)
        socialLoginDelegate?.onActivitySaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (email.isEmpty()) {
            displayLastEmailId()
        }
        socialLoginDelegate?.onActivityStarted()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        activityLoginBinding.emailEt.setText(savedInstanceState.getString("username"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tryToSetUIInteraction(true)
        socialLoginDelegate?.onActivityResult(requestCode, resultCode, data)
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
        activityLoginBinding.emailEt.setText(loginPrefs.lastAuthenticatedEmail)
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
            activityLoginBinding.passwordWrapper.error = getString(R.string.error_enter_password)
            activityLoginBinding.passwordEt.requestFocus()
        }
        if (email.isEmpty()) {
            activityLoginBinding.usernameWrapper.error = getString(R.string.error_enter_email)
            activityLoginBinding.emailEt.requestFocus()
        }
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activityLoginBinding.emailEt.isEnabled = false
            activityLoginBinding.passwordEt.isEnabled = false
            activityLoginBinding.forgotPasswordTv.isEnabled = false
            activityLoginBinding.endUserAgreementTv.isEnabled = false
            val loginTask: LoginTask = object : LoginTask(this, email, password) {
                override fun onPostExecute(result: AuthResponse?) {
                    super.onPostExecute(result)
                    if (result != null) {
                        onUserLoginSuccess()
                    }
                }

                override fun onException(ex: Exception) {
                    if (ex is HttpStatusException &&
                        ex.statusCode == HttpStatus.BAD_REQUEST
                    ) {
                        onUserLoginFailure(
                            LoginException(
                                LoginErrorMessage(
                                    getString(R.string.login_error),
                                    getString(R.string.login_failed)
                                )
                            ), null, null
                        )
                    } else {
                        onUserLoginFailure(ex, null, null)
                    }
                }
            }
            tryToSetUIInteraction(false)
            loginTask.setProgressDialog(activityLoginBinding.progress.progressIndicator)
            loginTask.execute()
        }
    }

    override fun onStop() {
        super.onStop()
        socialLoginDelegate?.onActivityStopped()
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
        task.setProgressDialog(activityLoginBinding.progress.progressIndicator)
    }

    override fun onUserLoginSuccess() {
        setResult(RESULT_OK)
        finish()
        val deepLink = intent.getParcelableExtra<DeepLink>(Router.EXTRA_DEEP_LINK)
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
                showAlertDialog(
                    errorMessage.messageLine1,
                    if (errorMessage.messageLine2 != null) errorMessage.messageLine2 else getString(
                        R.string.login_failed
                    )
                )
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
        activityLoginBinding.loginButtonLayout.isEnabled = enable
        invalidateTouch(enable)
        activityLoginBinding.loginBtnTv.text =
            getString(if (enable) R.string.login else R.string.signing_in)
        activityLoginBinding.socialAuth.facebookButton.isClickable = enable
        activityLoginBinding.socialAuth.googleButton.isClickable = enable
        activityLoginBinding.socialAuth.microsoftButton.isClickable = enable
        activityLoginBinding.emailEt.isEnabled = enable
        activityLoginBinding.passwordEt.isEnabled = enable
        activityLoginBinding.forgotPasswordTv.isEnabled = enable
        activityLoginBinding.endUserAgreementTv.isEnabled = enable
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
