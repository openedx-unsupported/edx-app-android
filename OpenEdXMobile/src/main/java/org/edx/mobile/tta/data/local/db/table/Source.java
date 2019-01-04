package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "source")
public class Source
{
    private long created_by;

    @PrimaryKey
    private long id;

    private String icon;

    private String title;

    private long modified_by;

    private String name;

    private String created_at;

    private String type;

    private String modified_at;

    public long getCreated_by ()
    {
        return created_by;
    }

    public void setCreated_by (long created_by)
    {
        this.created_by = created_by;
    }

    public long getId ()
    {
        return id;
    }

    public void setId (long id)
    {
        this.id = id;
    }

    public String getIcon ()
    {
        return icon;
    }

    public void setIcon (String icon)
    {
        this.icon = icon;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public long getModified_by ()
    {
        return modified_by;
    }

    public void setModified_by (long modified_by)
    {
        this.modified_by = modified_by;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getCreated_at ()
    {
        return created_at;
    }

    public void setCreated_at (String created_at)
    {
        this.created_at = created_at;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getModified_at ()
    {
        return modified_at;
    }

    public void setModified_at (String modified_at)
    {
        this.modified_at = modified_at;
    }

    @Override
    public String toString()
    {
        return "Source [created_by = "+created_by+", id = "+id+", icon = "+icon+", title = "+title+", modified_by = "+modified_by+", name = "+name+", created_at = "+created_at+", type = "+type+", modified_at = "+modified_at+"]";
    }
}
