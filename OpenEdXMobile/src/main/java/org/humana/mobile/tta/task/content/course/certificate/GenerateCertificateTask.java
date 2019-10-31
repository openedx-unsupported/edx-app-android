package org.humana.mobile.tta.task.content.course.certificate;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.content.CertificateStatusResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GenerateCertificateTask extends Task<CertificateStatusResponse> {

    private String courseId;

    @Inject
    private TaAPI taAPI;

    public GenerateCertificateTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public CertificateStatusResponse call() throws Exception {
        return taAPI.generateCertificate(courseId).execute().body();
    }
}
