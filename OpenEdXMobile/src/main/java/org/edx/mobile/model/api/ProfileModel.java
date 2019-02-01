package org.edx.mobile.model.api;

import java.io.Serializable;

public class ProfileModel implements Serializable {

    public Long id;
    public String username;
    public String email;
    public String name;
    public String course_enrollments;

    public String gender;
    public String title;
    public String classes_taught;
    public String state;
    public String diet_code;
    public String district;
    public String block;
    public String pmis_code;

    public String getState()
    {
        if(pmis_code==null)
            return "";
        else
            return pmis_code;
    }
    // public String url;
}
