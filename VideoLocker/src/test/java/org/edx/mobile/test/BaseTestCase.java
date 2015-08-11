package org.edx.mobile.test;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import roboguice.RoboGuice;

/**
 * Created by rohan on 12/31/14.
 */
@Ignore
@RunWith(RobolectricGradleTestRunner.class)
public class BaseTestCase {

    protected final Logger logger = new Logger(getClass().getName());

    protected Context context;
    protected Config config;
    protected CustomGuiceModule module;
    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        config = createConfig();

        module = new CustomGuiceModule();
        glueInjections();
        print("Started Test Case: " + getClass().getName());
    }

    private void glueInjections() {
        addBindings();
        if ( !module.isEmpty()  ) {
            Injector injector = RoboGuice.getOrCreateBaseApplicationInjector(RuntimeEnvironment.application, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), Modules.override(new EdxDefaultModule(context)).with(module));
            inject(injector);
        }
    }

    /**
     * subclass should inject the properties marked as @Inject
     */
    protected void inject(Injector injector ){}

    protected void addBindings() {
        module.addBinding(Config.class, config);
    }

    protected Config createConfig(){
        // Set up a new config instance that serves the mock host url
        JsonObject properties;
        try {
            InputStream in = context.getAssets().open("config/config.json");
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(new InputStreamReader(in));
            properties = config.getAsJsonObject();
        } catch (Exception e) {
            properties = new JsonObject();
            logger.error(e);
        }
        return new Config(properties);
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
