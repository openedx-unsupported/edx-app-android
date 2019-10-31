package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUsersTask extends Task<List<ProgramUser>> {

    private String programId, sectionId;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetUsersTask(Context context, String programId, String sectionId, int take, int skip) {
        super(context);
        this.programId = programId;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<ProgramUser> call() throws Exception {
        return taAPI.getUsers(programId, sectionId, take, skip).execute().body();
    }
}
