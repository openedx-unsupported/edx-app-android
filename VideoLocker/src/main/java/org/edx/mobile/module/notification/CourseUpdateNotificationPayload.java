package org.edx.mobile.module.notification;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Text;

import java.util.List;

/**
{
     "action": "course.announcement",
     "course-name": "edX Demonstration Course",
     "course-id": "edX/DemoX/Demo_Course",
     "push_hash": "d41d8cd98f00b204e9800998ecf8427e",
     "aps": {
     "content-available": 1,
     "alert": ""
     },
     "notification-id": "742e9881-e224-4ba9-b4d0-8a1f8b62b276"
}
 **/
public class CourseUpdateNotificationPayload extends BaseNotificationPayload{


    private @SerializedName("course-name") String courseName;
    private @SerializedName("course-id") String courseId;


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }


    public boolean isValid(){
        return !TextUtils.isEmpty(courseId);
    }


}
