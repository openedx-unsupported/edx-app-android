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
import org.humana.mobile.model.api.Mx_Response;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.tincan.model.Resume;

import java.util.List;

import retrofit2.Response;

public class TincanResumeTask extends Task<Mx_Response> {

    @NonNull
    private final List<Resume> resumeList;

    @NonNull
    private final Context ctx;
    @NonNull
    private Gson gson;

    @Inject
    private LoginAPI loginAPI;

    private MxAnalyticsAPI mxAnalyticsAPI;

   /* @Inject
    private AnalyticsAPI analyticsAPI;*/

    public TincanResumeTask(@NonNull Context context, @NonNull List<Resume> resumeList, MxAnalyticsAPI mAnalyticsAPI) {
        super(context);
        this.ctx = context;
        this.resumeList = resumeList;
        this.mxAnalyticsAPI=mAnalyticsAPI;
        gson=new Gson();
    }

    @Override
    @NonNull
    public Mx_Response call() throws Exception {
        Response<Mx_Response> response = mxAnalyticsAPI.postTincanResume(resumeList).execute();

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
