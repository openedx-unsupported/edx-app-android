package org.edx.mobile.tta.data.model.authentication;

import java.util.ArrayList;

public class StateCustomAttribute {

    private String placeholder;

    private ArrayList<Profession> profession;

    private String state;

    private String label;

    private String helptext;

    public String getPlaceholder ()
    {
        if(placeholder==null|| placeholder.isEmpty())
            placeholder="";

        return placeholder;
    }

    public void setPlaceholder (String placeholder)
    {
        this.placeholder = placeholder;
    }

    public ArrayList<Profession> getProfession ()
    {
        return profession;
    }

    public void setProfession (ArrayList<Profession> profession)
    {
        this.profession = profession;
    }

    public String getState ()
    {
        if(state==null|| state.isEmpty())
            state="";
        return state;
    }

    public void setState (String state)
    {
        this.state = state;
    }

    public String getLabel ()
    {
        if(label==null|| label.isEmpty())
            label="";

        return label;
    }

    public void setLabel (String label)
    {
        this.label = label;
    }

    public String getHelptext ()
    {
        if(helptext==null|| helptext.isEmpty())
            helptext="";

        return helptext;
    }

    public void setHelptext (String helptext)
    {
        this.helptext = helptext;
    }

    @Override
    public String toString()
    {
        return "StateCustomAttribute [placeholder = "+placeholder+", profession = "+profession+", state = "+state+", label = "+label+", helptext = "+helptext+"]";
    }

}
