package org.edx.mobile.test.feature;

import org.edx.mobile.test.feature.data.TestValues;
import org.edx.mobile.test.feature.interactor.AppInteractor;
import org.junit.Test;

public class LogInFeatureTest extends FeatureTest {

    @Test
    public void afterEmailLogIn_withActiveAccount_myCoursesScreenIsDisplayed() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen()
                .navigateToLogInScreen()
                .observeLogInScreen()
                .logIn(TestValues.ACTIVE_USER_CREDENTIALS)
                .observeMyCoursesScreen();
    }
}
