package org.edx.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetConfigurationTask extends Task<ConfigurationResponse> {

    @Inject
    private TaAPI taAPI;

    public GetConfigurationTask(Context context) {
        super(context);
    }

    @Override
    public ConfigurationResponse call() throws Exception {
        return taAPI.getConfiguration().execute().body();
    }
}
