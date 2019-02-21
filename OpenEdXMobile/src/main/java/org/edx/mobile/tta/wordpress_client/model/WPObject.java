package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.edx.mobile.tta.wordpress_client.util.Validate;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Arjun Singh
 *         Created on 2015/12/03.
 */
public abstract class WPObject<T extends WPObject> extends BaseModel {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_DATE = "date";
    public static final String JSON_FIELD_DATE_GMT = "date_gmt";
    public static final String JSON_FIELD_GUID = "guid";
    public static final String JSON_FIELD_MODIFIED = "modified";
    public static final String JSON_FIELD_MODIFIED_GMT = "modified_gmt";
    public static final String JSON_FIELD_SLUG = "slug";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_LINK = "link";
    public static final String JSON_FIELD_TITLE = "title";
    public static final String JSON_FIELD_AUTHOR = "author";
    public static final String JSON_FIELD_COMMENT_STATUS = "comment_status";
    public static final String JSON_FIELD_PING_STATUS = "ping_status";
    public static final String JSON_FIELD_LINKS = "_links";

    /**
     * Unique identifier for the object
     */
    @SerializedName("id")
    private long id = -1;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public abstract T withId(long id);

    /**
     * The date the object was published.
     */
    @SerializedName("date")
    private String date;

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public abstract T withDate(String date);

    /**
     * The date the object was published, as GMT.
     */
    @SerializedName("date_gmt")
    private String dateGMT;

    public void setDateGMT(String dateGMT) {
        this.dateGMT = dateGMT;
    }

    public String getDateGMT() {
        return dateGMT;
    }

    public abstract T withDateGMT(String dateGMT);

    /**
     * The globally unique identifier for the object.
     */
    @SerializedName("guid")
    private WPGeneric guid;

    public void setGuid(WPGeneric guid) {
        this.guid = guid;
    }

    public WPGeneric getGuid() {
        return guid;
    }

    public abstract T withGuid(WPGeneric guid);

    /**
     * The date the object was last modified.
     */
    @SerializedName("modified")
    private String modified;

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getModified() {
        return modified;
    }

    public abstract T withModified(String modified);

    /**
     * The date the object was last modified, as GMT.
     */
    @SerializedName("modified_gmt")
    private String modifiedGMT;

    public void setModifiedGMT(String modifiedGMT) {
        this.modifiedGMT = modifiedGMT;
    }

    public String getModifiedGMT() {
        return modifiedGMT;
    }

    public abstract T withModifiedGMT(String modifiedGMT);

    /**
     * An alphanumeric identifier for the object unique to its type.
     */
    @SerializedName("slug")
    private String slug;

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public abstract T withSlug(String slug);

    /**
     * Type of Post for the object.
     */
    @SerializedName("type")
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract T withType(String type);

    /**
     * URL to the object.
     */
    @SerializedName("link")
    private String link;

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public abstract T withLink(String link);

    /**
     * The title for the object.
     */
    @SerializedName("title")
    private WPGeneric title;

    public void setTitle(WPGeneric title) {
        this.title = title;
    }

    public WPGeneric getTitle() {
        return title;
    }

    public abstract T withTitle(String title);

    /**
     * The ID for the author of the object.
     */
    @SerializedName("author")
    private int author = -1;

    public void setAuthor(int author) {
        this.author = author;
    }

    public int getAuthor() {
        return author;
    }

    public abstract T withAuthor(int author);

    /**
     * Whether or not comments are open on the object.
     */
    @JsonAdapter(StatusDeserializer.class)
    @SerializedName("comment_status")
    private WPStatus commentStatus;

    public void setCommentStatus(WPStatus commentStatus) {
        this.commentStatus = commentStatus;
    }

    public WPStatus getCommentStatus() {
        return commentStatus;
    }

    public abstract T withCommentStatus(WPStatus commentStatus);

    /**
     * Whether or not the object can be pinged.
     */
    @JsonAdapter(StatusDeserializer.class)
    @SerializedName("ping_status")
    private WPStatus pingStatus;

    public void setPingStatus(WPStatus pingStatus) {
        this.pingStatus = pingStatus;
    }

    public WPStatus getPingStatus() {
        return pingStatus;
    }

    public abstract T withPingStatus(WPStatus pingStatus);

    /**
     * Links for this post; author, attachments, history, etc.
     */
    @JsonAdapter(LinksDeserializer.class)
    @SerializedName("_links")
    private ArrayList<Link> links;

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

    public void addLink(Link link) {
        if (links == null) {
            links = new ArrayList<>();
        }
        links.add(link);
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public abstract T withLinks(ArrayList<Link> links);

    public abstract T withLink(Link link);

    public WPObject() {
    }

    public WPObject(Parcel in) {
        super(in);
        id = in.readLong();
        date = in.readString();
        dateGMT = in.readString();
        guid = in.readParcelable(WPGeneric.class.getClassLoader());
        modified = in.readString();
        modifiedGMT = in.readString();
        slug = in.readString();
        type = in.readString();
        link = in.readString();
        title = in.readParcelable(WPGeneric.class.getClassLoader());
        author = in.readInt();
        commentStatus = in.readParcelable(WPStatus.class.getClassLoader());
        pingStatus = in.readParcelable(WPStatus.class.getClassLoader());
        in.readTypedList(links, Link.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(id);
        dest.writeString(date);
        dest.writeString(dateGMT);
        dest.writeParcelable(guid, flags);
        dest.writeString(modified);
        dest.writeString(modifiedGMT);
        dest.writeString(slug);
        dest.writeString(type);
        dest.writeString(link);
        dest.writeParcelable(title, flags);
        dest.writeInt(author);
        dest.writeParcelable(commentStatus, flags);
        dest.writeParcelable(pingStatus, flags);
        dest.writeTypedList(links);
    }

    public static <T extends WPObject> Map<String, Object> mapFromFields(WPObject<T> wpObject, Map<String, Object> builder) {
        Validate.validateMapLongEntry(JSON_FIELD_ID, wpObject.getId(), builder);
        Validate.validateMapEntry(JSON_FIELD_DATE, wpObject.getDate(), builder);
        Validate.validateMapEntry(JSON_FIELD_DATE_GMT, wpObject.getDateGMT(), builder);
        if (wpObject.getGuid() != null) {
            Validate.validateMapEntry(JSON_FIELD_GUID, wpObject.getGuid().getRendered(), builder);
        }
        Validate.validateMapEntry(JSON_FIELD_MODIFIED, wpObject.getModified(), builder);
        Validate.validateMapEntry(JSON_FIELD_MODIFIED_GMT, wpObject.getModifiedGMT(), builder);
        Validate.validateMapEntry(JSON_FIELD_SLUG, wpObject.getSlug(), builder);
        Validate.validateMapEntry(JSON_FIELD_TYPE, wpObject.getType(), builder);
        Validate.validateMapEntry(JSON_FIELD_LINK, wpObject.getLink(), builder);
        if (wpObject.getTitle() != null) {
            Validate.validateMapEntry(JSON_FIELD_TITLE, wpObject.getTitle().getRendered(), builder);
        }
        Validate.validateMapLongEntry(JSON_FIELD_AUTHOR, wpObject.getAuthor(), builder);
        if (wpObject.getCommentStatus() != null) {
            Validate.validateMapEntry(JSON_FIELD_COMMENT_STATUS, wpObject.getCommentStatus().getStatus(), builder);
        }
        if (wpObject.getPingStatus() != null) {
            Validate.validateMapEntry(JSON_FIELD_PING_STATUS, wpObject.getPingStatus().getStatus(), builder);
        }

        return builder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "WPObject{" +
                "id=" + id +
                "date='" + date + '\'' +
                ", dateGMT='" + dateGMT + '\'' +
                ", guid=" + guid +
                ", modified='" + modified + '\'' +
                ", modifiedGMT='" + modifiedGMT + '\'' +
                ", slug='" + slug + '\'' +
                ", type='" + type + '\'' +
                ", link='" + link + '\'' +
                ", title=" + title +
                ", author=" + author +
                ", commentStatus=" + commentStatus +
                ", pingStatus=" + pingStatus +
                ", links=" + links +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WPObject<?> wpObject = (WPObject<?>) o;

        if (id != wpObject.id) return false;
        if (!slug.equals(wpObject.slug)) return false;
        return type.equals(wpObject.type);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + slug.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
