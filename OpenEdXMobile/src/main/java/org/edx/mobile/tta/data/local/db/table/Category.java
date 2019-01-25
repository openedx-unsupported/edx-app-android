package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "category")
public class Category implements Comparable<Category>
{
    private long created_by;

    @PrimaryKey
    private long id;

    private String icon;

    private long source_id;

    private long order;

    private long modified_by;

    private String name;

    private String created_at;

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

    public long getSource_id()
    {
        return source_id;
    }

    public void setSource_id(long source_id)
    {
        this.source_id = source_id;
    }

    public long getOrder ()
    {
        return order;
    }

    public void setOrder (long order)
    {
        this.order = order;
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
        return "Category [created_by = "+created_by+", id = "+id+", icon = "+icon+", source_id = "+ source_id +", order = "+order+", modified_by = "+modified_by+", name = "+name+", created_at = "+created_at+", modified_at = "+modified_at+"]";
    }

    @Override
    public int compareTo(Category o) {
        return Long.compare(order, o.order);
    }
}
