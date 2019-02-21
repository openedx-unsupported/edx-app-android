package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model used for local Media items
 *
 * @author Arjun Singh
 *         Created on 2016/03/24.
 */
public class MediaItem extends BaseModel {

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_EXT = "ext_media";

    /**
     * Id of Post on WP if uploaded
     */
    public long postId = -1;

    /**
     * Row id of Post object in database
     */
    public long postRowId = -1;

    /**
     * Id of Media item on WP if uploaded
     */
    public long mediaId = -1;

    /**
     * Row id for Media item (_id field)
     */
    public long mediaRowId = -1;

    public String type;
    public String uri;
    public String caption;
    public String externalUrl;
    public int uploadState;

    public MediaItem() {
    }

    public MediaItem(Parcel in) {
        super(in);

        postId = in.readLong();
        postRowId = in.readLong();
        mediaId = in.readLong();
        mediaRowId = in.readLong();
        type = in.readString();
        uri = in.readString();
        caption = in.readString();
        externalUrl = in.readString();
        uploadState = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeLong(postId);
        dest.writeLong(postRowId);
        dest.writeLong(mediaId);
        dest.writeLong(mediaRowId);
        dest.writeString(type);
        dest.writeString(uri);
        dest.writeString(caption);
        dest.writeString(externalUrl);
        dest.writeInt(uploadState);
    }

    public String getMimeType() {
        if (type.equals(TYPE_IMAGE)) {
            return "image/jpeg";
        } else if (type.equals(TYPE_VIDEO)) {
            return "video/mp4";
        } else if (type.equals(TYPE_AUDIO)) {
            return "audio/mp3";
        }

        throw new IllegalStateException("No type on media item!");
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "postId=" + postId +
                ", postRowId=" + postRowId +
                ", mediaId=" + mediaId +
                ", type='" + type + '\'' +
                ", uri='" + uri + '\'' +
                ", caption='" + caption + '\'' +
                ", externalUrl='" + externalUrl + '\'' +
                '}';
    }

    public static final Parcelable.Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaItem mediaItem = (MediaItem) o;

        if (!type.equals(mediaItem.type)) return false;
        return uri.equals(mediaItem.uri);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }
}
