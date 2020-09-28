package org.humana.mobile.tta.analytics;

import org.humana.mobile.http.constants.ApiConstants;
import org.humana.mobile.model.api.Mx_Response;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.tincan.model.Resume;

import java.util.ArrayList;
import java.util.List;

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

    @POST(ApiConstants.URL_TINCAN_RESUME_BATCH)
    Call<Mx_Response> postTincanResume(@Body List<Resume> resumeList);
}
