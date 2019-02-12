package org.edx.mobile.tta.scorm;

import com.google.inject.Inject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static org.edx.mobile.http.constants.ApiConstants.URL_MX_SCORM_START;

public interface ScormService {

    class Provider implements com.google.inject.Provider<ScormService> {
        @Inject
        private Retrofit retrofit;

        @Override
        public ScormService get() {
            return retrofit.create(ScormService.class);
        }
    }

    @FormUrlEncoded
    @POST(URL_MX_SCORM_START)
    Call<ScormStartResponse> scormStart(@FieldMap Map<String, String> parameters);

}
