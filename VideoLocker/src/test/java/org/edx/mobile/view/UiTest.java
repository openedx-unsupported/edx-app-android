package org.edx.mobile.view;

import org.edx.mobile.test.http.HttpBaseTestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

/**
 * Base class for all UI test suites.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class UiTest extends HttpBaseTestCase {
    /**
     * Ensure login before tests.
     */
    @Before
    @Override
    public void login() throws Exception {
        super.login();
    }
}
