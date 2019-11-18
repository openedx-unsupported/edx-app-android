package org.humana.mobile.tta.ui.mxCalenderView;

public class Events  {

    private String DATE;
    private String title;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String eventText) {
        this.title = eventText;
    }

    public Events(String DATE, String eventText, String type) {
        this.DATE = DATE;
        this.title = eventText;
        this.type = type;

    }



    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }



}
