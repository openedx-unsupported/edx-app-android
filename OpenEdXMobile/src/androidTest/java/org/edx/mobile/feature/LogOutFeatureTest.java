package org.edx.mobile.feature;

import org.edx.mobile.feature.data.TestValues;
import org.edx.mobile.feature.interactor.AppInteractor;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
public class LogOutFeatureTest extends FeatureTest {

    @Test
    public void afterLogOut_withActiveAccount_logInScreenIsDisplayed() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen()
                .navigateToLogInScreen()
                .logIn(TestValues.ACTIVE_USER_CREDENTIALS)
                .observeMyCoursesScreen()
                .navigateToProfileScreen()
                .observeProfileScreen()
                .logOut()
                .observeLandingScreen();
    }
}
