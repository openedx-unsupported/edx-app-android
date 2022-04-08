package org.edx.mobile.discovery.model;

import java.util.Arrays;

public class ActionError {
    private String[] errors;
    private String action;

    public String[] getErrors() {
        return errors;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ActionError{" +
                "errors=" + Arrays.toString(errors) +
                ", action='" + action + '\'' +
                '}';
    }
}

