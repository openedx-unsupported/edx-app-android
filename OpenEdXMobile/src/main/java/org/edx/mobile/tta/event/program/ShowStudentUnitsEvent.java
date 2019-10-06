package org.edx.mobile.tta.event.program;

import org.edx.mobile.tta.data.model.program.ProgramUser;

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
