package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.RoomWarnings;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "content")
public class Content implements Parcelable {
    private long created_by;

    @PrimaryKey
    private long id;

    private String icon;

    @Embedded(prefix = "source_")
    private Source source;

    private long modified_by;

    @NonNull
    private String source_identity;

    private String name;

    private String created_at;

    private String modified_at;

    private List<Long> lists;

    private String detail;

    public Content() {
    }

    protected Content(Parcel in) {
        created_by = in.readLong();
        id = in.readLong();
        icon = in.readString();
        source = in.readParcelable(Source.class.getClassLoader());
        modified_by = in.readLong();
        source_identity = in.readString();
        name = in.readString();
        created_at = in.readString();
        modified_at = in.readString();
        if (lists == null){
            lists = new ArrayList<>();
        }
        in.readList(lists, Long.class.getClassLoader());
    }

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
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

    public Source getSource ()
    {
        return source;
    }

    public void setSource (Source source)
    {
        this.source = source;
    }

    public long getModified_by ()
    {
        return modified_by;
    }

    public void setModified_by (long modified_by)
    {
        this.modified_by = modified_by;
    }

    @NonNull
    public String getSource_identity ()
    {
        return source_identity;
    }

    public void setSource_identity (@NonNull String source_identity)
    {
        this.source_identity = source_identity;
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

    public List<Long> getLists ()
    {
        return lists;
    }

    public void setLists (List<Long> lists)
    {
        this.lists = lists;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString()
    {
        return "Content [created_by = "+created_by+", id = "+id+", icon = "+icon+", source = "+source+", modified_by = "+modified_by+", source_identity = "+source_identity+", name = "+name+", created_at = "+created_at+", modified_at = "+modified_at+", lists = "+lists+"]";
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
        dest.writeParcelable(source, flags);
        dest.writeLong(modified_by);
        dest.writeString(source_identity);
        dest.writeString(name);
        dest.writeString(created_at);
        dest.writeString(modified_at);
        dest.writeList(lists);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof Content) && (id == ((Content) obj).id);
    }
}
