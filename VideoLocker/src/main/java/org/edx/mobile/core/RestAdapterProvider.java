package org.edx.mobile.core;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.discussion.RetroHttpExceptionHandler;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;

import okhttp3.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RestAdapterProvider implements Provider<RestAdapter> {

    @Inject
    Config config;

    @Inject
    OkHttpClient client;

    @Override
    public RestAdapter get() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT)
                .serializeNulls()
                .create();

        return new RestAdapter.Builder()
                .setClient(new Ok3Client(client))
                .setEndpoint(config.getApiHostURL())
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new RetroHttpExceptionHandler())
                .build();
    }
}
