package org.edx.mobile.model.api;

import java.io.Serializable;

/**
 * Created by lindaliu on 7/13/15.
 */


public class CoursewareAccess implements Serializable {
    boolean has_access;
    AccessError error_code;
    String developer_message;
    String user_message;

    public CoursewareAccess(boolean has_access, AccessError error_code, String developer_message, String user_message) {
        this.has_access = has_access;
        this.error_code = error_code;
        this.developer_message = developer_message;
        this.user_message = user_message;
    }

    public AccessError getError_code() {
        return error_code;
    }

    public String getUser_message() {
        return user_message;
    }

    public boolean hasAccess() {
        return has_access;
    }
}
