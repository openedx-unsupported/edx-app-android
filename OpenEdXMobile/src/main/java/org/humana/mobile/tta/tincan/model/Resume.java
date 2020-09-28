package org.humana.mobile.tta.tincan.model;

public class Resume {

    private String Unit_id;

    private String User_Id;

    private String Id;

    private String Resume_Payload;

    private String Course_Id;

    private String activity_url;

    public String getUnit_id()
    {
        return Unit_id;
    }

    public void setUnit_id(String Unit_Id)
    {
        this.Unit_id = Unit_Id;
    }

    public String getUser_Id ()
    {
        return User_Id;
    }

    public void setUser_Id (String User_Id)
    {
        this.User_Id = User_Id;
    }

    public String getId ()
    {
        return Id;
    }

    public void setId (String Id)
    {
        this.Id = Id;
    }

    public String getResume_Payload ()
    {
        return Resume_Payload;
    }

    public void setResume_Payload (String Resume_Payload)
    {
        this.Resume_Payload = Resume_Payload;
    }

    public String getCourse_Id ()
    {
        return Course_Id;
    }

    public void setCourse_Id (String Course_Id)
    {
        this.Course_Id = Course_Id;
    }

    public String getActivity_url() {
        return activity_url;
    }

    public void setActivity_url(String activity_url) {
        this.activity_url = activity_url;
    }

    @Override
    public String toString()
    {
        return "Resume [Unit_id = "+ Unit_id +", User_Id = "+User_Id+", Id = "+Id+", Resume_Payload = "+Resume_Payload+", Course_Id = "+Course_Id+"]";
    }

}
