package org.edx.mobile.test.feature;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.test.feature.interactor.AppInteractor;
import org.junit.Test;

public class ExpiredOAuthTokenTest extends FeatureTest {

    @Test
    public void withInvalidToken_afterAppLaunch_redirectToLogin() {
        final MainApplication application = MainApplication.instance();
        PrefManager pref = new PrefManager(application, PrefManager.Pref.LOGIN);
        //Skip login if any profile is set
        pref.put(PrefManager.Key.PROFILE_JSON, "{\"id\": 1, \"username\": \"user\", \"email\": \"email@example.com\", \"name\": \"name\", \"course_enrollments\": \"https://mobile-devi.sandbox.edx.org/api/mobile/v0.5/users/staff/course_enrollments/\"}");
        pref.put(PrefManager.Key.AUTH_JSON, "{\"access_token\": \"I am an invalid token\", \"token_type\": \"Bearer\", \"expires_in\": 2591999, \"scope\": \"\"}");
        new AppInteractor()
                .launchApp()
                .skipToMyCoursesScreen()
                .redirectToLoginScreen()
                .observeLogInScreen();
    }
}
