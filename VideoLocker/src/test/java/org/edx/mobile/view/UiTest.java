package org.edx.mobile.view;

import com.google.gson.JsonObject;
import com.google.inject.Injector;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.test.http.HttpBaseTestCase;
import org.edx.mobile.util.Config;
import org.junit.Before;
import org.junit.Ignore;

import java.io.IOException;

/**
 * Base class for all UI test suites.
 */
@Ignore
public class UiTest extends HttpBaseTestCase {
    protected IEdxEnvironment environment;

    @Override
    protected JsonObject generateConfigProperties() throws IOException {
        // Set the new course navigation enabled config to true, since
        // our test suites were compiled against it.
        JsonObject properties = super.generateConfigProperties();
        properties.addProperty(Config.NEW_COURSE_NAVIGATION_ENABLED, true);
        return properties;
    }

    @Override
    protected void inject(Injector injector ){
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
