package org.edx.mobile.tta.data.remote.service;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.ModificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

//TaService
public interface TaService {

    class TaProvider implements Provider<TaService> {

        @Inject
        private Retrofit retrofit;

        @Override
        public TaService get() {
            return retrofit.create(TaService.class);
        }
    }

    @GET(ApiConstants.URL_MX_GET_CONFIGURATION)
    Call<ConfigurationResponse> getConfiguration();

    @GET(ApiConstants.URL_MX_GET_MODIFICATION)
    Call<ModificationResponse> getModification();

    @GET(ApiConstants.URL_MX_GET_CONTENTS)
    Call<List<Content>> getContents();

}
