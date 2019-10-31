package org.humana.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.content.TotalLikeResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class TotalLikeTask extends Task<TotalLikeResponse> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public TotalLikeTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public TotalLikeResponse call() throws Exception {
        return taAPI.totalLike(contentId).execute().body();
    }

}
