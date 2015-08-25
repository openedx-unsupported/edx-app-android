package org.edx.mobile.discussion;

import java.io.Serializable;
import java.util.Date;

/**
 * "blackouts": [{"start": "2015-04-15T00:00:00Z", "end": "2015-04-22T00:00:00Z"}],
 */
public class TimePeriod implements Serializable{
    private Date start;
    private Date end;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
