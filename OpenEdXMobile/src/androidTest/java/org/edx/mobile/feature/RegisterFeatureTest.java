package org.edx.mobile.feature;

import org.edx.mobile.feature.data.Credentials;
import org.edx.mobile.feature.interactor.AppInteractor;
import org.junit.Test;

public class RegisterFeatureTest extends FeatureTest {
    @Test
    public void afterRegistering_withFreshCredentials_myCoursesScreenIsDisplayed() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen()
                .navigateToRegistrationScreen()
                .observeRegistrationScreen()
                .createAccount(Credentials.freshCredentials(environment.getConfig()))
                .observeMyCoursesScreen();
    }
}
