package org.edx.mobile.tta.data.model.authentication;

public class RegistrationError {

    private String field_name;

    private String title;

    private String message;

    private String success;

    public String getField_name ()
    {
        return field_name;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setField_name (String field_name)
    {
        this.field_name = field_name;
    }

    public String getMessage ()
    {
        return message;
    }

    public void setMessage (String message)
    {
        this.message = message;
    }

    public String getSuccess ()
    {
        return success;
    }

    public void setSuccess (String success)
    {
        this.success = success;
    }

    @Override
    public String toString()
    {
        return "RegistrationError [field_name = "+field_name+", message = "+message+", success = "+success+"]";
    }

}
