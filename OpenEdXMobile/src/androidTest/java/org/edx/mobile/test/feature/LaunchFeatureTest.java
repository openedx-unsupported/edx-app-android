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
    public void whenAppLaunched_withInvalidProfile_landingScreenIsShown() {
        environment.getLoginPrefs().storeAuthTokenResponse(TestValues.INVALID_AUTH_TOKEN_RESPONSE, LoginPrefs.AuthBackend.PASSWORD);
        environment.getLoginPrefs().storeUserProfile(null); // Make sure that profile is null so we are going back to landing screen
        new AppInteractor()
                .launchApp()
                .observeLandingScreen(); // If profile is not set &
    }
}

