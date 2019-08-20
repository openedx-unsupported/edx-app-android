package org.edx.mobile.tta.event.program;

import org.edx.mobile.tta.data.local.db.table.Period;

public class PeriodSavedEvent {

    private Period period;

    public PeriodSavedEvent(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
