package org.edx.mobile.test;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;

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

        config = new Config(properties);

        module = new CustomGuiceModule();
        print("Started Test Case: " + getClass().getName());
    }

    protected void glueInjections(){
        addBindings();
        if ( !module.isEmpty()  ) {
            Injector injector = RoboGuice.getOrCreateBaseApplicationInjector(RuntimeEnvironment.application, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), module);
            inject(injector);
        }
    }

    /**
     * subclass should inject the properties marked as @Inject
     */
    protected void inject(Injector injector ){}

    protected void addBindings(){}

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
                binder.bind((Class<Object>) entry.getKey()).toInstance(entry.getValue());
            }
        }
    }
}
