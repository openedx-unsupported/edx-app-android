package org.edx.mobile.util;

import android.content.Context;

import org.edx.mobile.view.Router;

/**
 * Created by aleffert on 1/8/15.
 */
public class Environment {
    interface ConfigBuilder {
        Config make(Context context);
    }

    interface RouterBuilder {
        Router make();
    }

    private ConfigBuilder mConfigBuilder;
    private RouterBuilder mRouterBuilder;

    public Environment() {
        mConfigBuilder = new ConfigBuilder() {
            @Override
            public Config make(Context context) {
                return new Config(context);
            }
        };
        mRouterBuilder = new RouterBuilder() {
            @Override
            public Router make() {
                return new Router();
            }
        };
    }

    public void setConfigBuilder(ConfigBuilder builder) {
        mConfigBuilder = builder;
    }

    public void setRouterBuilder(RouterBuilder builder) {
        mRouterBuilder = builder;
    }

    public void setupEnvironment(Context context) {
        Config.setInstance(mConfigBuilder.make(context));
        Router.setInstance(mRouterBuilder.make());
    }

}
