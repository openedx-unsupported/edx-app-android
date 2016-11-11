package org.edx.mobile.test.feature;

import android.support.test.runner.AndroidJUnit4;

import com.google.inject.util.Modules;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.test.util.MockOverrideModule;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import roboguice.RoboGuice;

@RunWith(AndroidJUnit4.class)
public abstract class FeatureTest {
    protected EdxEnvironment environment;

    @Before
    public void setup() {
        // Ensure we are not logged in
        final MainApplication application = MainApplication.instance();
        RoboGuice.overrideApplicationInjector(application,
                Modules.override(new EdxDefaultModule(application.getApplicationContext()))
                        .with(new MockOverrideModule()));
        environment = application.getInjector().getInstance(EdxEnvironment.class);
        environment.getLoginPrefs().clear();
        environment.getAnalyticsRegistry().resetIdentifyUser();
    }
    @After
    public void removeMocks()
    {
        RoboGuice.Util.reset();
    }
}
