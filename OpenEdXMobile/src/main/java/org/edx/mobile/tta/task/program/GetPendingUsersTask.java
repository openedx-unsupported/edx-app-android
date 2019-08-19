package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.program.ProgramUser;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetPendingUsersTask extends Task<List<ProgramUser>> {

    private String programId, sectionId;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetPendingUsersTask(Context context, String programId, String sectionId, int take, int skip) {
        super(context);
        this.programId = programId;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<ProgramUser> call() throws Exception {
        return taAPI.getPendingUsers(programId, sectionId, take, skip).execute().body();
    }

}
