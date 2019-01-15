package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.edx.mobile.BR;

import androidx.annotation.Nullable;

@Entity(tableName = "content_list")
public class ContentList implements Comparable<ContentList>
{
    private String region;

    private long created_by;

    private String sort_as;

    private String sort_by;

    private String mode;

    @PrimaryKey
    private long id;

    private String auto_function;

    private long category;

    private long order;

    private long modified_by;

    private String name;

    private String format_type;

    private String created_at;

    private String modified_at;

    public String getRegion ()
    {
        return region;
    }

    public void setRegion (String region)
    {
        this.region = region;
    }

    public long getCreated_by ()
    {
        return created_by;
    }

    public void setCreated_by (long created_by)
    {
        this.created_by = created_by;
    }

    public String getSort_as ()
    {
        return sort_as;
    }

    public void setSort_as (String sort_as)
    {
        this.sort_as = sort_as;
    }

    public String getSort_by ()
    {
        return sort_by;
    }

    public void setSort_by (String sort_by)
    {
        this.sort_by = sort_by;
    }

    public String getMode ()
    {
        return mode;
    }

    public void setMode (String mode)
    {
        this.mode = mode;
    }

    public long getId ()
    {
        return id;
    }

    public void setId (long id)
    {
        this.id = id;
    }

    public String getAuto_function ()
    {
        return auto_function;
    }

    public void setAuto_function (String auto_function)
    {
        this.auto_function = auto_function;
    }

    public long getCategory ()
    {
        return category;
    }

    public void setCategory (long category)
    {
        this.category = category;
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

    public String getFormat_type ()
    {
        return format_type;
    }

    public void setFormat_type (String format_type)
    {
        this.format_type = format_type;
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
        return "ContentList [region = "+region+", created_by = "+created_by+", sort_as = "+sort_as+", sort_by = "+sort_by+", mode = "+mode+", id = "+id+", auto_function = "+auto_function+", category = "+category+", order = "+order+", modified_by = "+modified_by+", name = "+name+", format_type = "+format_type+", created_at = "+created_at+", modified_at = "+modified_at+"]";
    }

    @Override
    public int compareTo(ContentList o) {
        return Long.compare(order, o.order);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof ContentList) && (((ContentList) obj).id == id);
    }
}
