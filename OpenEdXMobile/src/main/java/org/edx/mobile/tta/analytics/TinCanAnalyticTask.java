package org.edx.mobile.tta.analytics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.SuccessResponse;

import retrofit2.Response;

/**
 * Created by JARVICE on 06-06-2018.
 */

public abstract class TinCanAnalyticTask extends Task<SuccessResponse> {

    @NonNull
    private final TincanRequest tincanAnalyticslist;

    @NonNull
    private final Context ctx;
    @NonNull
    private Gson gson;

    @Inject
    private LoginAPI loginAPI;

    @Inject
    private MxAnalyticsAPI mxAnalyticsAPI;

   /* @Inject
    private AnalyticsAPI analyticsAPI;*/

    public TinCanAnalyticTask(@NonNull Context context, @NonNull TincanRequest stringArrayList) {
        super(context);
        this.ctx = context;
        this.tincanAnalyticslist = stringArrayList;
        gson=new Gson();
    }

    @Override
    @NonNull
    public SuccessResponse call() throws Exception {
        Response<SuccessResponse> response = mxAnalyticsAPI.postTincanAnalytics(tincanAnalyticslist).execute();

        if (!response.isSuccessful()) {
            final int errorCode = response.code();
            final String errorBody = response.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new LoginAPI.RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return response.body();
    }
}
