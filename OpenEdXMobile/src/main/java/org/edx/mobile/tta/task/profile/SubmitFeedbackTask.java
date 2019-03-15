package org.edx.mobile.tta.task.profile;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.profile.FeedbackResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class SubmitFeedbackTask extends Task<FeedbackResponse> {

    private Bundle parameters;

    @Inject
    private TaAPI taAPI;

    public SubmitFeedbackTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public FeedbackResponse call() throws Exception {
        return taAPI.submitFeedback(parameters).execute().body();
    }
}
