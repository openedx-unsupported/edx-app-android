package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arjun Singh
 *         Created on 2016/01/14.
 */
public class Comment extends BaseModel {

    /**
     * Unique identifier for the object.
     */
    @SerializedName("id")
    private long id;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Comment withId(long id) {
        setId(id);
        return this;
    }

    /**
     * The id of the user object, if author was a user.
     */
    @SerializedName("author")
    private Long author;

    public void setAuthor(Long author) {
        this.author = author;
    }

    public Long getAuthor() {
        return author;
    }

    public Comment withAuthor(Long author) {
        setAuthor(author);
        return this;
    }

    /**
     * Avatar URLs for the object author.
     */
    @SerializedName("author_avatar_urls")
    private Map<String, String> authorAvatarUrls = new HashMap<>();

    public void setAuthorAvatarUrls(Map<String, String> map) {
        authorAvatarUrls = map;
    }

    public void addAuthorAvatarUrl(String key, String value) {
        authorAvatarUrls.put(key, value);
    }

    public Map<String, String> getAuthorAvatarUrls() {
        return authorAvatarUrls;
    }

    public String getAuthorAvatarUrl(String key) {
        return authorAvatarUrls.get(key);
    }

    public Comment withAuthorAvatarUrls(Map<String, String> map) {
        setAuthorAvatarUrls(map);
        return this;
    }

    public Comment withAuthorAvatarUrl(String key, String value) {
        addAuthorAvatarUrl(key, value);
        return this;
    }

    /**
     * Email address for the object author.
     */
    @SerializedName("author_email")
    private String authorEmail;

    public void setAuthorEmail(String email) {
        authorEmail = email;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public Comment withAuthorEmail(String email) {
        setAuthorEmail(email);
        return this;
    }

    /**
     * IP address for the object author.
     */
    @SerializedName("author_ip")
    private String authorIp;

    public void setAuthorIp(String authorIp) {
        this.authorIp = authorIp;
    }

    public String getAuthorIp() {
        return authorIp;
    }

    public Comment withAuthorIp(String authorIp) {
        setAuthorIp(authorIp);
        return this;
    }

    /**
     * Display name for the object author.
     */
    @SerializedName("author_name")
    private String authorName;

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Comment withAuthorName(String authorName) {
        setAuthorName(authorName);
        return this;
    }

    /**
     * URL for the object author.
     */
    @SerializedName("author_url")
    private String authorUrl;

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public Comment withAuthorUrl(String authorUrl) {
        setAuthorUrl(authorUrl);
        return this;
    }

    /**
     * User agent for the object author.
     */
    @SerializedName("author_user_agent")
    private String authorUserAgent;

    public void setAuthorUserAgent(String authorUserAgent) {
        this.authorUserAgent = authorUserAgent;
    }

    public String getAuthorUserAgent() {
        return authorUserAgent;
    }

    public Comment withAuthorUserAgent(String authorUserAgent) {
        setAuthorUserAgent(authorUserAgent);
        return this;
    }

    /**
     * The content for the object.
     */
    @SerializedName("content")
    private WPGeneric content;

    public void setContent(WPGeneric content) {
        this.content = content;
    }

    public WPGeneric getContent() {
        return content;
    }

    public Comment withContent(WPGeneric content) {
        setContent(content);
        return this;
    }

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

    public Comment withDate(String date) {
        setDate(date);
        return this;
    }

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

    public Comment withDateGMT(String dateGMT) {
        setDateGMT(dateGMT);
        return this;
    }

    /**
     * Karma for the object.
     */
    @SerializedName("karma")
    private int karma;

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public int getKarma() {
        return karma;
    }

    public Comment withKarma(int karma) {
        setKarma(karma);
        return this;
    }

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

    public Comment withLink(String link) {
        setLink(link);
        return this;
    }

    /**
     * The id for the parent of the object.
     */
    @SerializedName("parent")
    private Long parent;

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Long getParent() {
        return parent;
    }

    public Comment withParent(Long parent) {
        setParent(parent);
        return this;
    }

    /**
     * The id of the associated post object.
     */
    @SerializedName("post")
    private long post;

    public void setPost(long post) {
        this.post = post;
    }

    public long getPost() {
        return post;
    }

    public Comment withPost(long post) {
        setPost(post);
        return this;
    }

    /**
     * State of the object.
     */
    @SerializedName("status")
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Comment withStatus(String status) {
        setStatus(status);
        return this;
    }

    /**
     * Type of Comment for the object.
     */
    @SerializedName("type")
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Comment withType(String type) {
        setType(type);
        return this;
    }

    public Comment() {
    }

    public Comment(Parcel in) {
        super(in);
        id = in.readLong();
        author = in.readLong();
        in.readMap(authorAvatarUrls, String.class.getClassLoader());
        authorEmail = in.readString();
        authorIp = in.readString();
        authorName = in.readString();
        authorUrl = in.readString();
        authorUserAgent = in.readString();
        content = in.readParcelable(WPGeneric.class.getClassLoader());
        date = in.readString();
        dateGMT = in.readString();
        karma = in.readInt();
        link = in.readString();
        parent = in.readLong();
        post = in.readInt();
        status = in.readString();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(id);
        dest.writeLong(author);
        dest.writeMap(authorAvatarUrls);
        dest.writeString(authorEmail);
        dest.writeString(authorIp);
        dest.writeString(authorName);
        dest.writeString(authorUrl);
        dest.writeString(authorUserAgent);
        dest.writeParcelable(content, flags);
        dest.writeString(date);
        dest.writeString(dateGMT);
        dest.writeInt(karma);
        dest.writeString(link);
        dest.writeLong(parent);
        dest.writeLong(post);
        dest.writeString(status);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", author=" + author +
                ", authorAvatarUrls=" + authorAvatarUrls +
                ", authorEmail='" + authorEmail + '\'' +
                ", authorIp='" + authorIp + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorUrl='" + authorUrl + '\'' +
                ", authorUserAgent='" + authorUserAgent + '\'' +
                ", content=" + content +
                ", date='" + date + '\'' +
                ", dateGMT='" + dateGMT + '\'' +
                ", karma=" + karma +
                ", link='" + link + '\'' +
                ", parent=" + parent +
                ", post=" + post +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Comment && (((Comment) obj).id == id);
    }
}
