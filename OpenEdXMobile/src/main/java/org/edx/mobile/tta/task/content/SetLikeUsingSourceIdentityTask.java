package org.edx.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class SetLikeUsingSourceIdentityTask extends Task<StatusResponse> {

    private String sourceIdentity;

    @Inject
    private TaAPI taAPI;

    public SetLikeUsingSourceIdentityTask(Context context, String sourceIdentity) {
        super(context);
        this.sourceIdentity = sourceIdentity;
    }

    @Override
    public StatusResponse call() throws Exception {
        return taAPI.setLikeUsingSourceIdentity(sourceIdentity).execute().body();
    }

}
