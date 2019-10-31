package org.humana.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.library.CollectionConfigResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

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
