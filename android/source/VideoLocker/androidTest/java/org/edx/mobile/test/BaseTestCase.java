package org.edx.mobile.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import org.edx.mobile.logger.OEXLogger;

/**
 * Created by rohan on 12/31/14.
 */
public class BaseTestCase extends InstrumentationTestCase {

    protected final OEXLogger logger = new OEXLogger(getClass().getName());

    @Override
    protected void setUp() throws Exception {
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
