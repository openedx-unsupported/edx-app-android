package org.humana.mobile.tta.ui.mxCalenderView;

public class Events  {

    private String DATE;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String eventText) {
        this.title = eventText;
    }

    public Events(String DATE, String eventText) {
        this.DATE = DATE;
        this.title = eventText;

    }



    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }



}
