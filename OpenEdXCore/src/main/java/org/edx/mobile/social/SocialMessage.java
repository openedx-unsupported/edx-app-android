package org.edx.mobile.social;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SocialMessage implements Parcelable {
    protected long id;
    protected String message;
    protected SocialMember author;
    protected List<SocialComment> comments;

    public SocialMessage(long id, String message, SocialMember author) {
        this.id = id;
        this.message = message;
        this.author = author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.message);
        dest.writeParcelable(this.author, 0);
        dest.writeList(this.comments);
    }

    private SocialMessage(Parcel in) {
        this.id = in.readLong();
        this.message = in.readString();
        this.author = in.readParcelable(SocialMember.class.getClassLoader());
        this.comments = new ArrayList<SocialComment>();
        in.readList(this.comments, SocialComment.class.getClassLoader());
    }

    public static final Parcelable.Creator<SocialMessage> CREATOR = new Parcelable.Creator<SocialMessage>() {
        public SocialMessage createFromParcel(Parcel source) {
            return new SocialMessage(source);
        }

        public SocialMessage[] newArray(int size) {
            return new SocialMessage[size];
        }
    };
}
