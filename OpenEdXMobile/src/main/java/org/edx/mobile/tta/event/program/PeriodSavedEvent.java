package org.edx.mobile.tta.event.program;

import org.edx.mobile.tta.data.local.db.table.Period;

public class PeriodSavedEvent {

    private long periodId, unitsCountChange;

    public PeriodSavedEvent(long periodId, long unitsCountChange) {
        this.periodId = periodId;
        this.unitsCountChange = unitsCountChange;
    }

    public long getPeriodId() {
        return periodId;
    }

    public void setPeriodId(long periodId) {
        this.periodId = periodId;
    }

    public long getUnitsCountChange() {
        return unitsCountChange;
    }

    public void setUnitsCountChange(long unitsCountChange) {
        this.unitsCountChange = unitsCountChange;
    }
}
