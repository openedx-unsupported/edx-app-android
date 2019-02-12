package org.edx.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetConfigModifiedDateTask extends Task<ConfigModifiedDateResponse> {

    @Inject
    private TaAPI taAPI;

    public GetConfigModifiedDateTask(Context context) {
        super(context);
    }

    @Override
    public ConfigModifiedDateResponse call() throws Exception {
        return taAPI.getConfigModifiedDate().execute().body();
    }
}
