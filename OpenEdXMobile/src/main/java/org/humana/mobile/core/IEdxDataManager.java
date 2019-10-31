package org.humana.mobile.core;

import android.os.Environment;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.course.CourseAPI;
import org.humana.mobile.tta.data.remote.api.TaAPI;
import org.humana.mobile.user.UserAPI;

public interface IEdxDataManager {
    Environment getEnvironment();

    LoginAPI getLoginAPI();

    CourseAPI getCourseAPI();

    UserAPI getUserAPI();

    TaAPI getTaAPI();
}
