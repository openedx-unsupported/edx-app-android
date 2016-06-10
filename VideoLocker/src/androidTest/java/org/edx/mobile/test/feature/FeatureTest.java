package org.edx.mobile.test.feature;

import android.support.test.runner.AndroidJUnit4;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.module.prefs.PrefManager;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public abstract class FeatureTest {
    protected EdxEnvironment environment;

    @Before
    public void setup() {
        // Ensure we are not logged in
        final MainApplication application = MainApplication.instance();
        environment = application.getInjector().getInstance(EdxEnvironment.class);
        environment.getLoginPrefs().clear();
        environment.getSegment().resetIdentifyUser();
    }
}
