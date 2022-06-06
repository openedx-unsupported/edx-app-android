package org.edx.mobile.feature;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.feature.data.TestValues;
import org.edx.mobile.feature.interactor.AppInteractor;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.junit.Test;

import javax.inject.Inject;

public class LaunchFeatureTest extends FeatureTest {

    @Inject
    LoginAPI loginAPI;

    @Test
    public void whenAppLaunched_withAnonymousUser_landingScreenIsShown() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen();
    }

    @Test
    public void whenAppLaunched_withValidUser_myCoursesScreenIsShown() throws Exception {
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
