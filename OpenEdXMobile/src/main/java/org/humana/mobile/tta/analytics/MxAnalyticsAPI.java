package org.humana.mobile.tta.analytics;
import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.humana.mobile.model.api.Mx_Response;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.tincan.model.Resume;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * Created by mukesh on 25/4/18.
 */
@Singleton
public class MxAnalyticsAPI {

    @NonNull
    private MxAnalyticsService mService;

    @Inject
    public MxAnalyticsAPI(@NonNull AnalyticsRetrofitProvider retrofitProvider) {
        mService=retrofitProvider.getAnalyticsRetrofit().create(MxAnalyticsService.class);
    }

    public Call<SuccessResponse> postMxAnalytics(ArrayList<AnalyticModel> analyticModelList){

        return mService.postMxAnalytics(analyticModelList);
    }

    public Call<SuccessResponse> postTincanAnalytics(TincanRequest stringArrayList){

        return mService.postTincanAnalytics(stringArrayList);
    }
    public Call<Mx_Response> postTincanResume(List<Resume> resumeList){
        return mService.postTincanResume(resumeList);
    }
}
