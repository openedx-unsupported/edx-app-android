package org.edx.mobile.view.login;

import android.support.annotation.NonNull;

import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.ViewHoldingPresenter;

public class LoginPresenter extends ViewHoldingPresenter<LoginPresenter.LoginViewInterface> {

    final private Config config;
    final private NetworkUtil.ZeroRatedNetworkInfo networkInfo;

    public LoginPresenter(Config config, NetworkUtil.ZeroRatedNetworkInfo networkInfo) {
        this.config = config;
        this.networkInfo = networkInfo;
    }

    @Override
    public void attachView(@NonNull LoginViewInterface view) {
        super.attachView(view);

        if (networkInfo.isOnZeroRatedNetwork()) {
            view.setSocialLoginButtons(false, false);
        } else {
            view.setSocialLoginButtons(config.getGoogleConfig().isEnabled(), config.getFacebookConfig().isEnabled());
        }

        if (!config.isRegistrationEnabled()) {
            view.disableToolbarNavigation();
        }
    }

    public interface LoginViewInterface {

        void setSocialLoginButtons(boolean googleEnabled, boolean facebookEnabled);

        void disableToolbarNavigation();

    }
}
