package org.humana.mobile.tta.event.program;

import org.humana.mobile.tta.data.model.program.ProgramUser;

public class ShowStudentUnitsEvent {

    private ProgramUser user;

    public ShowStudentUnitsEvent(ProgramUser user) {
        this.user = user;
    }

    public ProgramUser getUser() {
        return user;
    }

    public void setUser(ProgramUser user) {
        this.user = user;
    }
}
