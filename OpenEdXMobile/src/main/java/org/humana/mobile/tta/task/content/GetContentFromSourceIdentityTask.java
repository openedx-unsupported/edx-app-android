package org.humana.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetContentFromSourceIdentityTask extends Task<Content> {

    private String sourceIdentity;

    @Inject
    private TaAPI taAPI;

    public GetContentFromSourceIdentityTask(Context context, String sourceIdentity) {
        super(context);
        this.sourceIdentity = sourceIdentity;
    }

    @Override
    public Content call() throws Exception {
        return taAPI.getContentFromSourceIdentity(sourceIdentity).execute().body();
    }

}
