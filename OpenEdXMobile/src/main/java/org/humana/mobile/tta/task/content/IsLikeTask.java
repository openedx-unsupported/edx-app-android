package org.humana.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.StatusResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class IsLikeTask extends Task<StatusResponse> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public IsLikeTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public StatusResponse call() throws Exception {
        return taAPI.isLike(contentId).execute().body();
    }

}
