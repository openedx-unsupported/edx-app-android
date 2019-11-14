package org.humana.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.CalendarEvents;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetEventCalenderTask extends Task<List<CalendarEvents>> {

    private String programId, sectionId, role;
    private long eventCalendarDate;
    private int count, take, skip;

    @Inject
    private TaAPI taAPI;

    public GetEventCalenderTask(Context context, String programId, String sectionId, String role,
                                Long eventCalendarDate, int count, int take , int skip) {
        super(context);
        this.programId = programId;
        this.sectionId = sectionId;
        this.role = role;
        this.eventCalendarDate = eventCalendarDate;
        this.count = count;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<CalendarEvents> call() throws Exception {
        return taAPI.getCalenderEvents(programId, sectionId, role, eventCalendarDate, count,take, skip ).execute().body();
    }
}
