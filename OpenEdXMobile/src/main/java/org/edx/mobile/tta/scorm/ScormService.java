package org.edx.mobile.tta.scorm;

import com.google.inject.Inject;

import org.edx.mobile.http.provider.RetrofitProvider;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static org.edx.mobile.http.constants.ApiConstants.URL_MX_SCORM_START;

public interface ScormService {

    class Provider implements com.google.inject.Provider<ScormService> {
        @Inject
        private RetrofitProvider retrofitProvider;

        @Override
        public ScormService get() {
            return retrofitProvider.getWithOfflineCache().create(ScormService.class);
        }
    }

    @FormUrlEncoded
    @POST(URL_MX_SCORM_START)
    Call<ScormStartResponse> scormStart(@FieldMap Map<String, String> parameters);

}
