package org.edx.mobile.core;

import android.os.Environment;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.tta.data.remote.api.TaAPI;
import org.edx.mobile.user.UserAPI;

public interface IEdxDataManager {
    Environment getEnvironment();

    LoginAPI getLoginAPI();

    CourseAPI getCourseAPI();

    UserAPI getUserAPI();

    TaAPI getTaAPI();
}
