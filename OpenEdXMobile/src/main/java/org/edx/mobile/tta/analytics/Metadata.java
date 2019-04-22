package org.edx.mobile.tta.analytics;

public class Metadata {

    private String content_id;
    private String source_identity;
    private String content_title;
    private String source_title;
    private String content_icon;

    private String threadId;
    private String threadTitle;
    private String topicType;

    private String commentId;
    private String commentTitle;
    private String commentType;

    private String user_id;
    private String user_display_name;
    private String user_icon;
    private String user_classes;

    private long likes;
    private long comments;

    private String scormId;
    private String scormTitle;
    private String scormSystemName;

    public String getContent_id() {
        return content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getSource_identity() {
        return source_identity;
    }

    public void setSource_identity(String source_identity) {
        this.source_identity = source_identity;
    }

    public String getContent_title() {
        return content_title;
    }

    public void setContent_title(String content_title) {
        this.content_title = content_title;
    }

    public String getSource_title() {
        return source_title;
    }

    public void setSource_title(String source_title) {
        this.source_title = source_title;
    }

    public String getContent_icon() {
        return content_icon;
    }

    public void setContent_icon(String content_icon) {
        this.content_icon = content_icon;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_display_name() {
        return user_display_name;
    }

    public void setUser_display_name(String user_display_name) {
        this.user_display_name = user_display_name;
    }

    public String getUser_icon() {
        return user_icon;
    }

    public void setUser_icon(String user_icon) {
        this.user_icon = user_icon;
    }

    public String getUser_classes() {
        return user_classes;
    }

    public void setUser_classes(String user_classes) {
        this.user_classes = user_classes;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public String getTopicType() {
        return topicType;
    }

    public void setTopicType(String topicType) {
        this.topicType = topicType;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentTitle() {
        return commentTitle;
    }

    public void setCommentTitle(String commentTitle) {
        this.commentTitle = commentTitle;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public String getScormId() {
        return scormId;
    }

    public void setScormId(String scormId) {
        this.scormId = scormId;
    }

    public String getScormTitle() {
        return scormTitle;
    }

    public void setScormTitle(String scormTitle) {
        this.scormTitle = scormTitle;
    }

    public String getScormSystemName() {
        return scormSystemName;
    }

    public void setScormSystemName(String scormSystemName) {
        this.scormSystemName = scormSystemName;
    }
}
