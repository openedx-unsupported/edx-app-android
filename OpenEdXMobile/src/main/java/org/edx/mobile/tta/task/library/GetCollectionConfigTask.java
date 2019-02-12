package org.edx.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetCollectionConfigTask extends Task<CollectionConfigResponse> {

    @Inject
    private TaAPI taAPI;

    public GetCollectionConfigTask(Context context) {
        super(context);
    }

    @Override
    public CollectionConfigResponse call() throws Exception {
        return taAPI.getCollectionConfig().execute().body();
    }
}
