package org.edx.mobile.test;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.edx.mobile.Injector;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.test.util.TimeUtilsForTests;
import org.edx.mobile.util.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TimeZone;

@Ignore
public abstract class BaseTestCase extends BaseTest {
    protected final Logger logger = new Logger(getClass().getName());

    protected Context context;
    protected Config config;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        config = new Config(generateConfigProperties());

        Injector injector = new Injector(config);
        inject(injector);
        // Set time zone to a constant value to make time-based tests predictable
        TimeZone.setDefault(TimeUtilsForTests.DEFAULT_TIME_ZONE);
        print("Started Test Case: " + getClass().getName());
    }

    /**
     * subclass should inject the properties marked as @Inject
     */
    protected void inject(Injector injector) throws Exception {
    }

    protected JsonObject generateConfigProperties() throws IOException {
        // Generate default config properties for subclasses to customize
        try (InputStream in = context.getAssets().open("config/config.json")) {
            return new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
        }
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
