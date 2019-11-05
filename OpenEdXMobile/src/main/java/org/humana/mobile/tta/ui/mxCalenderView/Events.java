package org.humana.mobile.tta.ui.mxCalenderView;

public class Events  {

    private String DATE;
    private String eventText;

    public String getEventText() {
        return eventText;
    }

    public void setEventText(String eventText) {
        this.eventText = eventText;
    }

    public Events(String DATE, String eventText) {
        this.DATE = DATE;
        this.eventText = eventText;

    }



    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }



}
