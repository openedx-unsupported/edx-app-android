package org.edx.mobile.tta.data.model.authentication;

import java.util.ArrayList;

public class FieldInfo {

    public String name;
    private ArrayList<StateCustomAttribute> attribute;

    public ArrayList<StateCustomAttribute> getStateCustomAttribute ()
    {
        return attribute;
    }

    public void setStateCustomAttribute (ArrayList<StateCustomAttribute> StateCustomAttribute)
    {
        this.attribute = StateCustomAttribute;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [StateCustomAttribute = "+attribute+"]";
    }

}
