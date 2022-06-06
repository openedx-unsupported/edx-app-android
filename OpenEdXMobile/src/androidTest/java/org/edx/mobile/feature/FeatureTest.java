package org.edx.mobile.feature;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.edx.mobile.core.EdxEnvironment;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(AndroidJUnit4.class)
public abstract class FeatureTest {

    @Inject
    protected EdxEnvironment environment;

    @Before
    public void setup() {
        environment.getLoginPrefs().clear();
        environment.getAnalyticsRegistry().resetIdentifyUser();
    }
}
