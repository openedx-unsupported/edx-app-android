package org.edx.mobile.tta.task.dashboard;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.ModificationResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetModificationTask extends Task<ModificationResponse> {

    @Inject
    private TaAPI taAPI;

    public GetModificationTask(Context context) {
        super(context);
    }

    @Override
    public ModificationResponse call() throws Exception {
        return taAPI.getModification().execute().body();
    }
}
