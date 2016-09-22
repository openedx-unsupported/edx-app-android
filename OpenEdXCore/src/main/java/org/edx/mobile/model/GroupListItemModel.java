package org.edx.mobile.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupListItemModel implements Parcelable {

    private String groupID;
    private String groupDescription;
    private String groupIconURI;
    private String groupName;


    public GroupListItemModel(String id, String name, String description, String iconUI){

        this.groupIconURI = iconUI;
        this.groupDescription = description;
        this.groupName = name;
        this.groupID = id;

    }

    public String getGroupID() {
        return groupID;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public String getGroupIconURI() {
        return groupIconURI;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupID);
        dest.writeString(this.groupDescription);
        dest.writeString(this.groupIconURI);
        dest.writeString(this.groupName);
    }

    private GroupListItemModel(Parcel in) {
        this.groupID = in.readString();
        this.groupDescription = in.readString();
        this.groupIconURI = in.readString();
        this.groupName = in.readString();
    }

    public static final Parcelable.Creator<GroupListItemModel> CREATOR = new Parcelable.Creator<GroupListItemModel>() {
        public GroupListItemModel createFromParcel(Parcel source) {
            return new GroupListItemModel(source);
        }

        public GroupListItemModel[] newArray(int size) {
            return new GroupListItemModel[size];
        }
    };
}
