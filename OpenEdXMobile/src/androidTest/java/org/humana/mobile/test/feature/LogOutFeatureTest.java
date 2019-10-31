package org.humana.mobile.test.feature;

import org.humana.mobile.test.feature.data.TestValues;
import org.humana.mobile.test.feature.interactor.AppInteractor;
import org.junit.Test;

public class LogOutFeatureTest extends FeatureTest {

    @Test
    public void afterLogOut_withActiveAccount_logInScreenIsDisplayed() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen()
                .navigateToLogInScreen()
                .logIn(TestValues.ACTIVE_USER_CREDENTIALS)
                .openNavigationDrawer()
                .logOut()
                .observeLogInScreen();
    }
}
