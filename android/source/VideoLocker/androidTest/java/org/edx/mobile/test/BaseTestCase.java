package org.edx.mobile.test;

import android.test.InstrumentationTestCase;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Environment;

/**
 * Created by rohan on 12/31/14.
 */
public class BaseTestCase extends InstrumentationTestCase {

    protected final Logger logger = new Logger(getClass().getName());

    @Override
    protected void setUp() throws Exception {
        Environment environment = new Environment();
        environment.setupEnvironment(getInstrumentation().getContext());
        super.setUp();
        print("Started Test Case: " + getClass().getName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        print("Finished Test Case: " + getClass().getName());
    }

    protected void print(String msg) {
        System.out.println(msg);
        logger.debug(msg);
    }
}
