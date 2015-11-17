package org.edx.mobile.view;

import com.google.inject.Injector;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.test.http.HttpBaseTestCase;
import org.junit.Before;
import org.junit.Ignore;

/**
 * Base class for all UI test suites.
 */
@Ignore
public class UiTest extends HttpBaseTestCase {
    protected IEdxEnvironment environment;

    @Override
    protected void inject(Injector injector) {
        super.inject(injector);
        environment = injector.getInstance(IEdxEnvironment.class);
    }

    /**
     * Ensure login before tests.
     */
    @Before
    @Override
    public void login() throws Exception {
        super.login();
    }
}
