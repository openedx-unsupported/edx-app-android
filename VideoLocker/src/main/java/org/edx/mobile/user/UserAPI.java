package org.edx.mobile.user;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.OkHttpClient;

import org.edx.mobile.discussion.RetroHttpExceptionHandler;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;

import java.util.Collections;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

@Singleton
public class UserAPI {

    private UserService userService;

    @Inject
    public UserAPI(@NonNull Config config, @NonNull OkHttpClient client) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(config.getApiHostURL())
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new RetroHttpExceptionHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        userService = restAdapter.create(UserService.class);
    }

    public Account getAccount(@NonNull String username) throws RetroHttpException {
        return userService.getAccount(username);
    }

    public Account updateAccount(@NonNull String username, @NonNull String field, @NonNull Object value) throws RetroHttpException {
        return userService.updateAccount(username, Collections.singletonMap(field, value));
    }
}
