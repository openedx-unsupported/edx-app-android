package org.edx.mobile.view.login

import org.edx.mobile.util.Config
import org.edx.mobile.util.NetworkUtil.ZeroRatedNetworkInfo
import org.edx.mobile.view.ViewHoldingPresenter
import org.edx.mobile.view.login.LoginPresenter.LoginViewInterface

class LoginPresenter constructor(
    private val config: Config,
    private val networkInfo: ZeroRatedNetworkInfo
) : ViewHoldingPresenter<LoginViewInterface>() {

    override fun attachView(view: LoginViewInterface) {
        super.attachView(view)
        if (networkInfo.isOnZeroRatedNetwork) {
            view.setSocialLoginButtons(
                googleEnabled = false,
                facebookEnabled = false,
                microsoftEnabled = false
            )
        } else {
            view.setSocialLoginButtons(
                googleEnabled = config.googleConfig.isEnabled,
                facebookEnabled = config.facebookConfig.isEnabled,
                microsoftEnabled = config.microsoftConfig.isEnabled
            )
        }
        if (!config.isRegistrationEnabled) {
            view.disableToolbarNavigation()
        }
    }

    interface LoginViewInterface {
        fun setSocialLoginButtons(
            googleEnabled: Boolean,
            facebookEnabled: Boolean,
            microsoftEnabled: Boolean
        )

        fun disableToolbarNavigation()
    }
}
