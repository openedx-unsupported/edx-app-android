package org.edx.mobile.login;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.edx.mobile.base.PresenterTest;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.login.LoginPresenter;
import org.junit.Test;
import org.mockito.Mock;

public class LoginPresenterTest extends PresenterTest<LoginPresenter, LoginPresenter.LoginViewInterface> {

    @Mock
    Config config;

    @Test
    public void testOnViewCreation_withGoogleLoginDisabled_googleButtonNotVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(false));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(true, "dummy app id", "client_token"));
        when(config.getMicrosoftConfig()).thenReturn(new Config.MicrosoftConfig(true));
        startPresenter(new LoginPresenter(config));
        verify(view).setSocialLoginButtons(false, true, true);
    }

    @Test
    public void testOnViewCreation_withFacebookLoginDisabled_facebookButtonNotVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(true));
        when(config.getMicrosoftConfig()).thenReturn(new Config.MicrosoftConfig(true));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(false, "dummy app id", "client_token"));
        startPresenter(new LoginPresenter(config));
        verify(view).setSocialLoginButtons(true, false, true);
    }

    @Test
    public void testOnViewCreation_withMicrosoftLoginDisabled_microsoftButtonNotVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(true));
        when(config.getMicrosoftConfig()).thenReturn(new Config.MicrosoftConfig(false));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(true, "dummy app id", "client_token"));
        startPresenter(new LoginPresenter(config));
        verify(view).setSocialLoginButtons(true, true, false);
    }

    @Test
    public void testOnViewCreation_withSocialLoginEnabled_socialLoginButtonsVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(true));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(true, "dummy app id", "client_token"));
        when(config.getMicrosoftConfig()).thenReturn(new Config.MicrosoftConfig(true));
        startPresenter(new LoginPresenter(config));
        verify(view).setSocialLoginButtons(true, true, true);
    }

    @Test
    public void testOnViewCreation_withZeroRatedNetwork_socialLoginButtonsNotVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(true));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(true, "dummy app id", "client_token"));
        when(config.getMicrosoftConfig()).thenReturn(new Config.MicrosoftConfig(true));
        startPresenter(new LoginPresenter(config));
        verify(view).setSocialLoginButtons(false, false, false);
    }
}
