package org.humana.mobile.tta.task.content.course.certificate;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.content.MyCertificatesResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetCertificateTask extends Task<MyCertificatesResponse> {

    private String courseId;

    @Inject
    private TaAPI taAPI;

    public GetCertificateTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public MyCertificatesResponse call() throws Exception {
        return taAPI.getCertificate(courseId).execute().body();
    }
}
