package org.edx.mobile.tta.wordpress_client.model;

/**
 * Created by JARVICE on 10-01-2018.
 */

public class Like
{
    private String likes;

    private boolean is_liked;

    public String getLikes ()
    {
        return likes;
    }

    public void setLikes (String likes)
    {
        this.likes = likes;
    }

    public boolean getIs_liked ()
    {
        return is_liked;
    }

    public void setIs_liked (boolean is_liked)
    {
        this.is_liked = is_liked;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [likes = "+likes+", is_liked = "+is_liked+"]";
    }
}
