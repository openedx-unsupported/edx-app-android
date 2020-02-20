package org.humana.mobile.tta.data.model.program;

public class EventNotificationCount {


    public EventNotificationCount(Boolean isCountChanged) {
        this.isCountChanged = isCountChanged;
    }

    public Boolean isCountChanged() {
        return isCountChanged;
    }

    public void setCountChanged(Boolean countChanged) {
        isCountChanged = countChanged;
    }

    private Boolean isCountChanged;

}
