package org.edx.mobile.coursemultilingual;

import androidx.room.Entity;
import androidx.room.RoomWarnings;

import java.util.List;
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "coursemultilingual")
public class CourseMultilingualModel {
    public final static String COURSEMULTILINGUAL = "coursemultilingual";
    private String course_key;
    private String content_type;

    public String getCourse_key() {
        return course_key;
    }

    public void setCourse_key(String course_key) {
        this.course_key = course_key;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<CourseTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<CourseTranslation> translations) {
        this.translations = translations;
    }

    private String text;
    private List<CourseTranslation> translations;

}
