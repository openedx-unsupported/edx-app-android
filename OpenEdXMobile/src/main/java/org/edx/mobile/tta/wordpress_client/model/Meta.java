package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arjun Singh
 *         Created on 2016/01/13.
 */
public class Meta extends BaseModel {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_KEY = "key";
    public static final String JSON_FIELD_VALUE = "value";

    /**
     * Unique identifier for the object.
     */
    private long id = -1;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Meta withId(long id) {
        setId(id);
        return this;
    }

    /**
     * Id of item this object is linked to.
     */
    private long postId = -1;

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public long getPostId() {
        return postId;
    }

    public Meta withPostId(long postId) {
        setPostId(postId);
        return this;
    }

    /**
     * The key for the custom field.
     */
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Meta withKey(String key) {
        setKey(key);
        return this;
    }

    /**
     * The value of the custom field.
     */
    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Meta withValue(String value) {
        setValue(value);
        return this;
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();

        map.put(key, value);

        return map;
    }

    public Meta() {
    }

    public Meta(Parcel in) {
        super(in);
        id = in.readLong();
        key = in.readString();
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(id);
        dest.writeString(key);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Parcelable.Creator<Meta> CREATOR = new Creator<Meta>() {
        @Override
        public Meta createFromParcel(Parcel source) {
            return new Meta(source);
        }

        @Override
        public Meta[] newArray(int size) {
            return new Meta[size];
        }
    };

    @Override
    public String toString() {
        return "Meta{" +
                "id=" + id +
                ", postId=" + postId +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
