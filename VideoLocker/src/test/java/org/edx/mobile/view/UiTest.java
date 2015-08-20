package org.edx.mobile.view;

import org.edx.mobile.test.http.HttpBaseTestCase;
import org.junit.Before;

/**
 * Base class for all UI test suites.
 */
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
