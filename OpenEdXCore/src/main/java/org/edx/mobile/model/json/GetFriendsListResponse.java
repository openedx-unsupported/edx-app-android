package org.edx.mobile.model.json;

import android.os.Parcel;
import android.os.Parcelable;

import org.edx.mobile.social.SocialMember;

import java.util.List;

/**
 * Created by marcashman on 2014-12-17.
 */
public class GetFriendsListResponse implements Parcelable {

    private List<SocialMember> friends;

    public GetFriendsListResponse(List<SocialMember> friends) {
        this.friends = friends;
    }

    public List<SocialMember> getFriends() {
        return friends;
    }

    public void setFriends(List<SocialMember> friends) {
        this.friends = friends;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(friends);
    }

    public GetFriendsListResponse() {
    }

    private GetFriendsListResponse(Parcel in) {
        in.readTypedList(friends, SocialMember.CREATOR);
    }

    public static final Creator<GetFriendsListResponse> CREATOR = new Creator<GetFriendsListResponse>() {
        public GetFriendsListResponse createFromParcel(Parcel source) {
            return new GetFriendsListResponse(source);
        }

        public GetFriendsListResponse[] newArray(int size) {
            return new GetFriendsListResponse[size];
        }
    };
}
