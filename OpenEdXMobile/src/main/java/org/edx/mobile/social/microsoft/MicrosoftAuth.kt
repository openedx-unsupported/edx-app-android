package org.edx.mobile.social.microsoft

import android.app.Activity
import android.content.Intent
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import org.edx.mobile.R
import org.edx.mobile.social.ISocialImpl

class MicrosoftAuth(activity: Activity?) : ISocialImpl(activity) {
    private var microsoftClient: PublicClientApplication? = null
    override fun login() {
        microsoftClient = PublicClientApplication(this.activity, R.raw.auth_config)
        microsoftClient?.acquireToken(activity, SCOPES, object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                callback?.onLogin(authenticationResult.accessToken)
                logger.debug("Microsoft Logged in successfully.")
            }

            override fun onError(exception: MsalException) {
                callback?.onError(exception)
                logger.error(exception, true)
            }

            override fun onCancel() {
                callback?.onCancel()
                logger.debug("Microsoft Log in canceled.")
            }
        })
    }

    override fun logout() {
        microsoftClient?.getAccounts { accounts ->
            if (accounts.isNotEmpty()) {
                for (account in accounts) {
                    // Pass empty callback because sometime throw `NullPointerException`
                    // due to null callback
                    microsoftClient?.removeAccount(
                            account) { }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    companion object {
        private val SCOPES = arrayOf("https://graph.microsoft.com/User.Read")
    }
}
