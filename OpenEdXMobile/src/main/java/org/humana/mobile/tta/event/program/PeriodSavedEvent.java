package org.humana.mobile.tta.event.program;


public class PeriodSavedEvent {

    private long periodId;
    private long unitsCountChange;


    private long pointCountChange;

    public PeriodSavedEvent(long periodId, long unitsCountChange, long pointCountChange) {
        this.periodId = periodId;
        this.unitsCountChange = unitsCountChange;
        this.pointCountChange = pointCountChange;
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

    public long getPointCountChange() {
        return pointCountChange;
    }

    public void setPointCountChange(long pointCountChange) {
        this.pointCountChange = pointCountChange;
    }

}
