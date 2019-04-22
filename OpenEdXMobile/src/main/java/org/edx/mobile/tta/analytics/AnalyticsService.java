package org.edx.mobile.tta.analytics;

import com.google.inject.Inject;

import org.edx.mobile.http.constants.ApiConstants;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by manprax on 4/1/17.
 */

public interface  AnalyticsService {

    /**
     * A RoboGuice Provider implementation for LoginService.
     */
    class Provider implements com.google.inject.Provider<AnalyticsService> {
        @Inject
        private Retrofit retrofit;


        @Override
        public AnalyticsService get() {
            return retrofit.create(AnalyticsService.class);
        }
    }

    /*
     * If there are form validation errors, this call will fail with 400 or 409 error code.
     * In case of validation errors the response body will be {@link org.tta.mobile.model.api.FormFieldMessageBody}.
     */
    /*@Headers({
            "Content-Type:application/x-www-form-urlencoded",
            "Authorization:"
    })*/
    @FormUrlEncoded
    @POST(ApiConstants.URL_ANALYTIC_BATCH)
    Call<ResponseBody> updateAnalytics(@FieldMap Map<String, String> parameters);
}
