package org.humana.mobile.core;

import android.os.Environment;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.course.CourseAPI;
import org.humana.mobile.tta.data.remote.api.TaAPI;
import org.humana.mobile.user.UserAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EdxDataManager  implements  IEdxDataManager {
    @Inject
    Environment environment;

    @Inject
    LoginAPI loginAPI;

    @Inject
    CourseAPI courseAPI;

    @Inject
    UserAPI userAPI;

    @Inject
    TaAPI taAPI;
    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public LoginAPI getLoginAPI() {
        return loginAPI;
    }

    @Override
    public CourseAPI getCourseAPI() {
        return courseAPI;
    }

    @Override
    public UserAPI getUserAPI() {
        return userAPI;
    }

    @Override
    public TaAPI getTaAPI() {
        return taAPI;
    }
}
