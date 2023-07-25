package org.edx.mobile.view.login;

import androidx.annotation.NonNull;

import org.edx.mobile.util.Config;
import org.edx.mobile.view.ViewHoldingPresenter;

public class LoginPresenter extends ViewHoldingPresenter<LoginPresenter.LoginViewInterface> {

    final private Config config;

    public LoginPresenter(Config config) {
        this.config = config;
    }

    @Override
    public void attachView(@NonNull LoginViewInterface view) {
        super.attachView(view);
        view.setSocialLoginButtons(config.getGoogleConfig().isEnabled(),
                config.getFacebookConfig().isEnabled(),
                config.getMicrosoftConfig().isEnabled());

        if (!config.isRegistrationEnabled()) {
            view.disableToolbarNavigation();
        }
    }

    public interface LoginViewInterface {

        void setSocialLoginButtons(boolean googleEnabled, boolean facebookEnabled, boolean microsoftEnabled);

        void disableToolbarNavigation();
    }
}
