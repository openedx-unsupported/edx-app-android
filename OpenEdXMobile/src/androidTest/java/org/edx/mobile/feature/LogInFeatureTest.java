package org.edx.mobile.feature;

import org.edx.mobile.feature.data.TestValues;
import org.edx.mobile.feature.interactor.AppInteractor;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
public class LogInFeatureTest extends FeatureTest {

    @Test
    public void afterEmailLogIn_withActiveAccount_myCoursesScreenIsDisplayed() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen()
                .navigateToLogInScreen()
                .logIn(TestValues.ACTIVE_USER_CREDENTIALS)
                .observeMyCoursesScreen();
    }
}
