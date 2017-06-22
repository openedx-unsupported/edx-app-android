package org.edx.mobile.test.util;

import android.os.Bundle;

import com.google.inject.AbstractModule;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.test.feature.data.TestValues;
import org.edx.mobile.user.UserAPI;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import retrofit2.mock.Calls;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Make sure that basic modules are mocked so we can remove dependency on a
 * real edX install.
 */

public class MockOverrideModule extends AbstractModule {
    private final LoginAPI mockLoginAPI;
    private final CourseAPI mockCourseAPI;
    private final UserAPI mockUserAPI;

    public MockOverrideModule() {
        super();
        mockLoginAPI = mock(LoginAPI.class);
        mockCourseAPI = mock(CourseAPI.class);
        mockUserAPI = mock(UserAPI.class);

        try {
            when(mockLoginAPI.logInUsingEmail(TestValues.ACTIVE_USER_CREDENTIALS.email,
                    TestValues.ACTIVE_USER_CREDENTIALS.password))
                    .thenAnswer(new Answer<AuthResponse>() {
                        public AuthResponse answer(InvocationOnMock invocation) throws Throwable {
                            EdxEnvironment environment = MainApplication.instance().getInjector().getInstance(EdxEnvironment.class);
                            environment.getLoginPrefs().storeAuthTokenResponse(TestValues.VALID_AUTH_TOKEN_RESPONSE, LoginPrefs.AuthBackend.PASSWORD);
                            environment.getLoginPrefs().storeUserProfile(TestValues.DUMMY_PROFILE);
                            return (AuthResponse) TestValues.VALID_AUTH_TOKEN_RESPONSE;
                        }
                    });

            when(mockLoginAPI.registerUsingEmail(any(Bundle.class)))
                    .thenAnswer(new Answer<AuthResponse>() {
                        public AuthResponse answer(InvocationOnMock invocation) throws Throwable {
                            Bundle bundle = invocation.getArgumentAt(0, Bundle.class);
                            EdxEnvironment environment = MainApplication.instance().getInjector().getInstance(EdxEnvironment.class);
                            environment.getLoginPrefs().storeAuthTokenResponse(TestValues.VALID_AUTH_TOKEN_RESPONSE, LoginPrefs.AuthBackend.PASSWORD);
                            ProfileModel profile = new ProfileModel();
                            profile.name = bundle.getString("name");
                            profile.email = bundle.getString("email");
                            profile.username = bundle.getString("username");
                            profile.course_enrollments = bundle.getString("course_enrollments");
                            profile.id = bundle.getLong("id");
                            environment.getLoginPrefs().storeUserProfile(profile);
                            return (AuthResponse) TestValues.VALID_AUTH_TOKEN_RESPONSE;
                        }
                    });

            when(mockCourseAPI.getCourseList(anyInt()))
                    .thenReturn(Calls.response(TestValues.BASIC_COURSE_LIST));

            when(mockCourseAPI.getEnrolledCourses()).thenReturn(Calls.response(TestValues.BASIC_USER_ENROLLED_COURSES));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void configure() {
        bind(LoginAPI.class).toInstance(mockLoginAPI);
        bind(CourseAPI.class).toInstance(mockCourseAPI);
        bind(UserAPI.class).toInstance(mockUserAPI);
    }
}

