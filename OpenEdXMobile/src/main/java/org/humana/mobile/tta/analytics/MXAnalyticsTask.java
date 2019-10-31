package org.humana.mobile.tta.analytics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.http.HttpResponseStatusException;
import org.humana.mobile.http.HttpStatus;
import org.humana.mobile.model.api.FormFieldMessageBody;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.SuccessResponse;
import java.util.ArrayList;

import retrofit2.Response;

/**
 * Created by manprax on 4/1/17.
 */

public abstract class MXAnalyticsTask extends Task<SuccessResponse> {

    @NonNull
    private final ArrayList<AnalyticModel> analyticModelList;

    @NonNull
    private final Context ctx;
    @NonNull
    private  Gson gson;

    @Inject
    private LoginAPI loginAPI;

    @Inject
    private MxAnalyticsAPI mxAnalyticsAPI;

   /* @Inject
    private AnalyticsAPI analyticsAPI;*/

    public MXAnalyticsTask(@NonNull Context context, @NonNull ArrayList<AnalyticModel> analyticModelList) {
        super(context);
        this.ctx = context;
        this.analyticModelList = analyticModelList;
        gson=new Gson();
    }

    @Override
    @NonNull
    public SuccessResponse call() throws Exception {
        Response<SuccessResponse> response = mxAnalyticsAPI.postMxAnalytics(analyticModelList).execute();

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
