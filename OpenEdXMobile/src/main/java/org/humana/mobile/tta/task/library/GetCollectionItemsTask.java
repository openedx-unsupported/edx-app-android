package org.humana.mobile.tta.task.library;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.library.CollectionItemsResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetCollectionItemsTask extends Task<List<CollectionItemsResponse>> {

    private Bundle parameters;

    @Inject
    private TaAPI taAPI;

    public GetCollectionItemsTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public List<CollectionItemsResponse> call() throws Exception {
        return taAPI.getCollectionItems(parameters).execute().body();
    }
}
