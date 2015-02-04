package org.edx.mobile.social;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SocialGroup implements Parcelable {

    protected int unread;
    protected long id;
    protected String name;
    protected String description;
    protected List<SocialMember> members;

    public interface SocialMembersCallback {
        public void onSuccess(List<SocialMember> response);
        public void onError(SocialProvider.SocialError err);
    }

    public SocialGroup(long id, String name, String description, int unread) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unread = unread;
    }

    public SocialGroup(long id, String name, String description, int unread, List<SocialMember> members) {
        this(id, name, description, unread);
        this.members = new ArrayList<SocialMember>(members);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMembers(List<SocialMember> members) {
        this.members = members;
    }
    public List<SocialMember> getMembers() {
        return this.members;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeInt(this.unread);
        dest.writeList(this.members);
    }

    private SocialGroup(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.description = in.readString();
        this.unread = in.readInt();
        this.members = new ArrayList<SocialMember>();
        in.readList(this.members, SocialMember.class.getClassLoader());
    }

    public static final Parcelable.Creator<SocialGroup> CREATOR = new Parcelable.Creator<SocialGroup>() {
        public SocialGroup createFromParcel(Parcel source) {
            return new SocialGroup(source);
        }

        public SocialGroup[] newArray(int size) {
            return new SocialGroup[size];
        }
    };

    @Override
    public boolean equals(Object other){

        return (other instanceof SocialGroup && other != null && ((SocialGroup) other).getId() == this.getId());

    }

    @Override
    public int hashCode(){
        return Long.valueOf(getId()).hashCode();
    }
}
