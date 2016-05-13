package org.edx.mobile.core;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.http.RetroHttpExceptionHandler;
import org.edx.mobile.util.Config;

import okhttp3.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RestAdapterProvider implements Provider<RestAdapter> {

    @Inject
    Config config;

    @Inject
    OkHttpClient client;

    @Inject
    Gson gson;

    @Override
    public RestAdapter get() {
        return new RestAdapter.Builder()
                .setClient(new Ok3Client(client))
                .setEndpoint(config.getApiHostURL())
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new RetroHttpExceptionHandler())
                .build();
    }
}
