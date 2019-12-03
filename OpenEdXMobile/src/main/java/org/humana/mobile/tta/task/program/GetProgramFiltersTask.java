package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetProgramFiltersTask extends Task<List<ProgramFilter>> {

    @Inject
    private TaAPI taAPI;

    private String program_id, show_in;

    private String section_id;

    private List<ProgramFilter> filters;

    public GetProgramFiltersTask(Context context, String program_id, String show_in, String section_id,
                                 List<ProgramFilter> filters) {
        super(context);
        this.program_id = program_id;
        this.section_id = section_id;
        this.show_in = show_in;
        this.filters = filters;
    }

    @Override
    public List<ProgramFilter> call() throws Exception {
        return taAPI.getProgramFilters(program_id, section_id, show_in, filters).execute().body();
    }
}
