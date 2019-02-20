package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "source")
public class Source implements Parcelable
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

    public Source() {
    }

    protected Source(Parcel in) {
        created_by = in.readLong();
        id = in.readLong();
        icon = in.readString();
        title = in.readString();
        modified_by = in.readLong();
        name = in.readString();
        created_at = in.readString();
        type = in.readString();
        modified_at = in.readString();
    }

    public static final Creator<Source> CREATOR = new Creator<Source>() {
        @Override
        public Source createFromParcel(Parcel in) {
            return new Source(in);
        }

        @Override
        public Source[] newArray(int size) {
            return new Source[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(created_by);
        dest.writeLong(id);
        dest.writeString(icon);
        dest.writeString(title);
        dest.writeLong(modified_by);
        dest.writeString(name);
        dest.writeString(created_at);
        dest.writeString(type);
        dest.writeString(modified_at);
    }
}
