package org.edx.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.remote.api.TaAPI;

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
