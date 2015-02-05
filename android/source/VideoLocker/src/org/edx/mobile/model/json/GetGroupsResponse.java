package org.edx.mobile.model.json;

import android.os.Parcel;
import android.os.Parcelable;

import org.edx.mobile.social.SocialGroup;

import java.util.List;

/**
 * Created by marcashman on 2014-12-17.
 */
public class GetGroupsResponse implements Parcelable {

    private List<SocialGroup> groups;

    public GetGroupsResponse(List<SocialGroup> groups) {
        this.groups = groups;
    }

    public List<SocialGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<SocialGroup> groups) {
        this.groups = groups;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(groups);
    }

    private GetGroupsResponse(Parcel in) {
        in.readTypedList(groups, SocialGroup.CREATOR);
    }

    public static final Parcelable.Creator<GetGroupsResponse> CREATOR = new Parcelable.Creator<GetGroupsResponse>() {
        public GetGroupsResponse createFromParcel(Parcel source) {
            return new GetGroupsResponse(source);
        }

        public GetGroupsResponse[] newArray(int size) {
            return new GetGroupsResponse[size];
        }
    };
}
