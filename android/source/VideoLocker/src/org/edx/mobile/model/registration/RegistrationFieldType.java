package org.edx.mobile.model.registration;

public enum RegistrationFieldType {
    MULTI ("MULTI"),
    PASSWORD ("PASSWORD"),
    EMAIL ("EMAIL"),
    TEXT ("TEXT"),
    TEXTAREA ("TEXTAREA"),
    CHECKBOX ("CHECKBOX");

    private final String fieldType;

    private RegistrationFieldType(String name) {
        this.fieldType = name;
    }

    public String toString(){
        return fieldType;
    }
}
