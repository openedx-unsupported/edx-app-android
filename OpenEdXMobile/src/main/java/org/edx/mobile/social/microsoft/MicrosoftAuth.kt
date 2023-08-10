package org.edx.mobile.social.microsoft

import android.app.Activity
import android.content.Intent
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication.RemoveAccountCallback
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import org.edx.mobile.R
import org.edx.mobile.social.ISocialImpl

class MicrosoftAuth(activity: Activity?) : ISocialImpl(activity) {
    private var microsoftClient: IMultipleAccountPublicClientApplication? = null

    override fun login() {
        PublicClientApplication.createMultipleAccountPublicClientApplication(this.activity,
            R.raw.auth_config,
            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    microsoftClient = application
                    microsoftClient?.acquireToken(
                        AcquireTokenParameters.Builder()
                            .startAuthorizationFromActivity(activity)
                            .withScopes(SCOPES)
                            .withCallback(object : AuthenticationCallback {
                                override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                                    callback?.onLogin(authenticationResult?.accessToken)
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
                            .build()
                    )
                }

                override fun onError(exception: MsalException) {
                    logger.error(exception, true)
                    callback?.onError(exception)
                }
            })
    }

    override fun logout() {
        microsoftClient?.getAccounts(object : IPublicClientApplication.LoadAccountsCallback {
            override fun onTaskCompleted(result: MutableList<IAccount>?) {
                result?.forEach { account ->
                    microsoftClient?.removeAccount(account, object : RemoveAccountCallback {
                        override fun onRemoved() {
                            // No action needed
                        }

                        override fun onError(exception: MsalException) {
                            logger.error(exception, true)
                        }
                    })
                }
            }

            override fun onError(exception: MsalException?) {
                logger.error(exception, true)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    companion object {
        private val SCOPES = listOf("User.Read")
    }
}
