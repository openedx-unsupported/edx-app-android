package org.edx.mobile.test.feature;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.test.feature.data.TestValues;
import org.edx.mobile.test.feature.interactor.AppInteractor;
import org.junit.Test;

public class LaunchFeatureTest extends FeatureTest {

    @Test
    public void whenAppLaunched_withAnonymousUser_landingScreenIsShown() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen();
    }

    @Test
    public void whenAppLaunched_withValidUser_myCoursesScreenIsShown() throws Exception {
        final MainApplication application = MainApplication.instance();
        final LoginAPI loginAPI = application.getInjector().getInstance(LoginAPI.class);
        loginAPI.logInUsingEmail(TestValues.ACTIVE_USER_CREDENTIALS.email, TestValues.ACTIVE_USER_CREDENTIALS.password);
        new AppInteractor()
                .launchApp()
                .observeMyCoursesScreen();
    }

    @Test
    public void whenAppLaunched_withInvalidAuthToken_logInScreenIsShown() {
        environment.getLoginPrefs().storeAuthTokenResponse(TestValues.INVALID_AUTH_TOKEN_RESPONSE, LoginPrefs.AuthBackend.PASSWORD);
        environment.getLoginPrefs().storeUserProfile(TestValues.DUMMY_PROFILE);
        new AppInteractor()
                .launchApp()
                .observeLogInScreen()
                .navigateBack()
                .observeLandingScreen();
    }
}
