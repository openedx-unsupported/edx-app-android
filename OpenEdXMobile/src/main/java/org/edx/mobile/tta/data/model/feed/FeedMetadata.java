package org.edx.mobile.tta.data.model.feed;

import android.arch.persistence.room.Embedded;

import com.google.gson.annotations.SerializedName;

public class FeedMetadata {

    private String id;
    private String user_name;

    @Embedded(prefix = "user_icon_")
    private UserIcon user_icon;

    private String user_username;
    private String tag_label;
    private String icon;
    private String source_title;
    private String source_name;
    private long like_count;
    private long comment_count;
    private String text;
    private boolean liked;

    @SerializedName("share_link")
    private String share_url;

    private String course_key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public UserIcon getUser_icon() {
        return user_icon;
    }

    public void setUser_icon(UserIcon user_icon) {
        this.user_icon = user_icon;
    }

    public String getUser_username() {
        return user_username;
    }

    public void setUser_username(String user_username) {
        this.user_username = user_username;
    }

    public String getTag_label() {
        return tag_label;
    }

    public void setTag_label(String tag_label) {
        this.tag_label = tag_label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSource_title() {
        return source_title;
    }

    public void setSource_title(String source_title) {
        this.source_title = source_title;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public long getLike_count() {
        return like_count;
    }

    public void setLike_count(long like_count) {
        this.like_count = like_count;
    }

    public long getComment_count() {
        return comment_count;
    }

    public void setComment_count(long comment_count) {
        this.comment_count = comment_count;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public String getCourse_key() {
        return course_key;
    }

    public void setCourse_key(String course_key) {
        this.course_key = course_key;
    }
}
