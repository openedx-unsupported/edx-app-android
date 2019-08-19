package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetAllUnitsTask extends Task<List<CourseComponent>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId, searchText;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetAllUnitsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId, String searchText, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
        this.searchText = searchText;
    }

    @Override
    public List<CourseComponent> call() throws Exception {
        return taAPI.getAllUnits(filters, programId, sectionId, searchText, take, skip).execute().body();
    }

}
