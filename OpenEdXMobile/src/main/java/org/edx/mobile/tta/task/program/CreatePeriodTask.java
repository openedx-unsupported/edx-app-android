package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class CreatePeriodTask extends Task<SuccessResponse> {

    private String programId, sectionId, lang, periodName;

    @Inject
    private TaAPI taAPI;

    public CreatePeriodTask(Context context, String programId, String sectionId, String lang) {
        super(context);
        this.programId = programId;
        this.sectionId = sectionId;
//        this.periodName = periodName;
        this.lang = lang;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.createPeriod(programId, sectionId, lang).execute().body();
    }
}
