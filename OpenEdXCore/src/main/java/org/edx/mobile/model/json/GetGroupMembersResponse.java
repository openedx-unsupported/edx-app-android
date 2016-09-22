package org.edx.mobile.model.json;

import android.os.Parcel;
import android.os.Parcelable;

import org.edx.mobile.social.SocialMember;

import java.util.List;

/**
 * Created by marcashman on 2014-12-18.
 */
public class GetGroupMembersResponse implements Parcelable {

    private List<SocialMember> members;

    public GetGroupMembersResponse(List<SocialMember> friends) {
        this.members = friends;
    }

    public List<SocialMember> getMembers() {
        return members;
    }

    public void setMembers(List<SocialMember> members) {
        this.members = members;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(members);
    }

    private GetGroupMembersResponse(Parcel in) {
        in.readTypedList(members, SocialMember.CREATOR);
    }

    public static final Creator<GetGroupMembersResponse> CREATOR = new Creator<GetGroupMembersResponse>() {
        public GetGroupMembersResponse createFromParcel(Parcel source) {
            return new GetGroupMembersResponse(source);
        }

        public GetGroupMembersResponse[] newArray(int size) {
            return new GetGroupMembersResponse[size];
        }
    };
}
