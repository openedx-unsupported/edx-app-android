package org.humana.mobile.tta.data.local.db.table;


import org.humana.mobile.tta.ui.mxCalenderView.Events;

import java.util.List;

public class CalendarEvents {

    public CalendarEvents(long eventDate, List<Events> eventList) {
        this.eventDate = eventDate;
        this.eventList = eventList;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
    }

    public List<Events> getEventList() {
        return eventList;
    }

    public void setEventList(List<Events> eventList) {
        this.eventList = eventList;
    }

    public long eventDate;


    public List<Events> eventList;

}
