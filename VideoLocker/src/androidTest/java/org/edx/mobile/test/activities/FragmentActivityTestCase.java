package org.edx.mobile.test.activities;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.test.ActivityInstrumentationTestCase2;

import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.Environment;

/**
 * Created by hanning on 4/7/15.
 */
public class FragmentActivityTestCase extends ActivityInstrumentationTestCase2<BaseFragmentActivity> {
    protected final Logger logger = new Logger(getClass().getName());



    public FragmentActivityTestCase() {
        super( BaseFragmentActivity.class );
    }

    public FragmentActivityTestCase(Class<BaseFragmentActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        print("Started Test Case: " + getClass().getName());
    }


    public void testSomething() throws Exception {

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
