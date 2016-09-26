package org.edx.mobile.social;

import android.os.Parcel;
import android.os.Parcelable;


public class SocialComment implements Parcelable {
    protected long id;
    protected String message;
    protected SocialMember author;

    protected int numApprovals;

    public SocialComment(long id, String message, SocialMember author, int numApprovals) {
        this.id = id;
        this.message = message;
        this.author = author;
        this.numApprovals = numApprovals;
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
        dest.writeInt(this.numApprovals);
    }

    private SocialComment(Parcel in) {
        this.id = in.readLong();
        this.message = in.readString();
        this.author = in.readParcelable(SocialMember.class.getClassLoader());
        this.numApprovals = in.readInt();
    }

    public static final Parcelable.Creator<SocialComment> CREATOR = new Parcelable.Creator<SocialComment>() {
        public SocialComment createFromParcel(Parcel source) {
            return new SocialComment(source);
        }

        public SocialComment[] newArray(int size) {
            return new SocialComment[size];
        }
    };
}
