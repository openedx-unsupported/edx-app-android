package org.humana.mobile.tta.task.content.course.certificate;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.content.MyCertificatesResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetMyCertificatesTask extends Task<MyCertificatesResponse> {

    @Inject
    private TaAPI taAPI;

    public GetMyCertificatesTask(Context context) {
        super(context);
    }

    @Override
    public MyCertificatesResponse call() throws Exception {
        return taAPI.getMyCertificates().execute().body();
    }
}
