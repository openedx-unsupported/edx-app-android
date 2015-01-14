package org.edx.mobile.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

/**
 * Created by rohan on 12/31/14.
 */
public class BaseTestCase extends InstrumentationTestCase {

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
        Log.i("test", msg);
    }
}
