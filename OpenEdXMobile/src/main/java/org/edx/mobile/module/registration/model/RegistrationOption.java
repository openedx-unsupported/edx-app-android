package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

public class RegistrationOption {
    private @SerializedName("default")     boolean defaultValue;
    private @SerializedName("name")     String name;
    private @SerializedName("value")    String value;

    public RegistrationOption() {
    }

    public RegistrationOption(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public boolean equals(Object object){
        return (object instanceof RegistrationOption) &&
                toString().equals(((RegistrationOption) object).toString());
    }

    public int hashCode(){
        //we will use this method after we upgrade to jdk 1.7
      //  return super.hashCode(name, value);
        int hashcode = name == null ? 0 : name.hashCode() / 2;
        return  hashcode += ( value == null ? 0 : value.hashCode()/2 );
    }

    @Override
    public String toString() {
        return name;
    }
}
