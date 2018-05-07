package org.edx.mobile.test;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.test.util.TimeUtilsForTests;
import org.edx.mobile.util.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import roboguice.RoboGuice;

@Ignore
public abstract class BaseTestCase extends BaseTest {
    protected final Logger logger = new Logger(getClass().getName());

    protected Context context;
    protected Config config;
    protected CustomGuiceModule module;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        config = new Config(generateConfigProperties());

        module = new CustomGuiceModule();
        glueInjections();
        // Set time zone to a constant value to make time-based tests predictable
        TimeZone.setDefault(TimeUtilsForTests.DEFAULT_TIME_ZONE);
        print("Started Test Case: " + getClass().getName());
    }

    private void glueInjections() throws Exception {
        addBindings();
        if ( !module.isEmpty()  ) {
            RoboGuice.setUseAnnotationDatabases(false);
            Injector injector = RoboGuice.getOrCreateBaseApplicationInjector(RuntimeEnvironment.application, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), Modules.override(new EdxDefaultModule(context)).with(module));
            inject(injector);
        }
    }

    /**
     * subclass should inject the properties marked as @Inject
     */
    protected void inject(Injector injector) throws Exception {}

    protected void addBindings() {
        module.addBinding(Config.class, config);
    }

    protected JsonObject generateConfigProperties() throws IOException {
        // Generate default config properties for subclasses to customize
        InputStream in = context.getAssets().open("config/config.json");
        try {
            return new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
        } finally {
            in.close();
        }
    }



    @After
    public void tearDown() throws Exception {
        RoboGuice.Util.reset();
        print("Finished Test Case: " + getClass().getName());
    }

    protected void print(String msg) {
        System.out.println(msg);
        logger.debug(msg);
    }

    public class CustomGuiceModule extends AbstractModule {

        private HashMap<Class<?>, Object> bindings;


        public CustomGuiceModule() {
            super();
            bindings = new HashMap<Class<?>, Object>();
        }

        public boolean isEmpty(){
            return bindings.isEmpty();
        }


        public void addBinding(Class<?> type, Object object) {
            bindings.put(type, object);
        }

        @Override
        protected void configure() {
            Set<Map.Entry<Class<?>, Object>> entries = bindings.entrySet();
            for (Map.Entry<Class<?>, Object> entry : entries) {
                Class<Object> classToBind = (Class<Object>) entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Class) {
                    binder.bind(classToBind).to((Class) value);
                } else {
                    binder.bind(classToBind).toInstance(value);
                }
            }
        }
    }
}
