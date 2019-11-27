package org.edx.mobile.http.provider;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.edx.mobile.util.Config;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public interface RetrofitProvider extends Provider<Retrofit> {
    @NonNull Retrofit get();
    @NonNull Retrofit getWithOfflineCache();
    @NonNull Retrofit getNonOAuthBased();

    @Singleton
    class Impl implements RetrofitProvider {
        private static final int CLIENT_INDEX_DEFAULT = 0;
        private static final int CLIENT_INDEX_WITH_OFFLINE_CACHE = 1;
        private static final int CLIENT_INDEX_NON_OAUTH_BASED = 2;
        private static final int CLIENTS_COUNT = 3;

        @Inject
        private Config config;

        @Inject
        private OkHttpClientProvider clientProvider;

        @Inject
        private Gson gson;

        private Retrofit[] retrofits = new Retrofit[CLIENTS_COUNT];

        @NonNull
        @Override
        public Retrofit get() {
            return get(CLIENT_INDEX_DEFAULT, clientProvider.get());
        }

        @NonNull
        public Retrofit getWithOfflineCache() {
            return get(CLIENT_INDEX_WITH_OFFLINE_CACHE, clientProvider.getWithOfflineCache());
        }

        @NonNull
        public Retrofit getNonOAuthBased() {
            return get(CLIENT_INDEX_NON_OAUTH_BASED, clientProvider.getNonOAuthBased());
        }

        @NonNull
        private synchronized Retrofit get(final int index, @NonNull final OkHttpClient client) {
            Retrofit retrofit = retrofits[index];
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .client(client)
                        .baseUrl(config.getApiHostURL())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
                retrofits[index] = retrofit;
            }
            return retrofit;
        }
    }
}
