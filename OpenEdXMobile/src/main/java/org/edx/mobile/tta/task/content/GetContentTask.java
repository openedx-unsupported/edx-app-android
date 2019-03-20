package org.edx.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetContentTask extends Task<Content> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public GetContentTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public Content call() throws Exception {
        return taAPI.getContent(contentId).execute().body();
    }
}
