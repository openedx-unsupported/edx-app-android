package org.edx.mobile.test.feature;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.ServiceManager;
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
        final EdxEnvironment environment = application.getInjector().getInstance(EdxEnvironment.class);
        ServiceManager api = environment.getServiceManager();
        //Get and cache user login data before app launch
        api.auth(TestValues.ACTIVE_USER_CREDENTIALS.email, TestValues.ACTIVE_USER_CREDENTIALS.password);
        api.getProfile();

        new AppInteractor()
                .launchApp()
                .observeMyCoursesScreen();
    }

    @Test
    public void whenAppLaunched_withInvalidAuthToken_logInScreenIsShown() {
        final MainApplication application = MainApplication.instance();
        PrefManager pref = new PrefManager(application, PrefManager.Pref.LOGIN);
        //Skip login if any profile is set
        pref.put(PrefManager.Key.PROFILE_JSON, TestValues.DUMMY_PROFILE_JSON);
        pref.put(PrefManager.Key.AUTH_JSON, TestValues.INVALID_AUTH_JSON);
        new AppInteractor()
                .launchApp()
                .observeLogInScreen()
                .navigateBack()
                .observeLandingScreen();
    }
}
