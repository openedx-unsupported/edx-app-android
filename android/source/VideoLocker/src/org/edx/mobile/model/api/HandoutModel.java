package org.edx.mobile.model.api;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HandoutModel implements Serializable {

    public String handouts_html;

    public String getHandouts_html() {
        return handouts_html;
    }

    public void setHandouts_html(String handouts_html) {
        this.handouts_html = handouts_html;
    }
}
