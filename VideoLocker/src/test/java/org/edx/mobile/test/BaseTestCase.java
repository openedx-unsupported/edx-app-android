package org.edx.mobile.test;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by rohan on 12/31/14.
 */
@Ignore
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApplication.class)
public class BaseTestCase {

    protected final Logger logger = new Logger(getClass().getName());

    @Before
    public void setUp() throws Exception {
        Environment environment = new Environment();
        environment.setupEnvironment(RuntimeEnvironment.application);
        print("Started Test Case: " + getClass().getName());
    }

    @After
    public void tearDown() throws Exception {
        print("Finished Test Case: " + getClass().getName());
    }

    protected void print(String msg) {
        System.out.println(msg);
        logger.debug(msg);
    }
}
