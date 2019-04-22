package org.edx.mobile.tta.analytics;

import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.tta.data.model.SuccessResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by mukesh on 25/4/18.
 */

public interface MxAnalyticsService {

    @POST(ApiConstants.URL_ANALYTIC_BATCH)
    Call<SuccessResponse> postMxAnalytics(@Body ArrayList<AnalyticModel> analyticModelList);

    @POST(ApiConstants.URL_TINCAN_ANALYTIC_BATCH)
    Call<SuccessResponse> postTincanAnalytics(@Body TincanRequest analyticModelList);
}
