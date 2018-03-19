package org.edx.mobile.model.api;

import java.io.Serializable;

public class ProfileModel implements Serializable {

    public Long id;
    public String username;
    public String email;
    public String name;
    public Integer year_of_birth;
    public String course_enrollments;
    // public String url;
}
