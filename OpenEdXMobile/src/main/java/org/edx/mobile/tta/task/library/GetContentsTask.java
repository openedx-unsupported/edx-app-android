package org.edx.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.ContentResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetContentsTask extends Task<ContentResponse> {

    @Inject
    private TaAPI taAPI;

    public GetContentsTask(Context context) {
        super(context);
    }

    @Override
    public ContentResponse call() throws Exception {
        return taAPI.getContents().execute().body();
    }
}
