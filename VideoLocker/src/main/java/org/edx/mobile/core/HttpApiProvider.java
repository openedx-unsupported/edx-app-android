package org.edx.mobile.core;

import android.app.Application;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.http.Api;
import org.edx.mobile.http.IApi;
import org.edx.mobile.http.RestApiManager;
import org.edx.mobile.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class HttpApiProvider implements Provider<IApi> {

    @Inject
    Application application;
    @Inject
    Config config;

    @Override
    public IApi get() {
        if (MainApplication.RETROFIT_ENABLED ){
            return new RestApiManager(application);
        } else {
            return new Api(application);
        }
    }
}
