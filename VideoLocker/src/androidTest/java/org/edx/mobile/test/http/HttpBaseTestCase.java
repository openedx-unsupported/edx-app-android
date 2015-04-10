package org.edx.mobile.test.http;

import android.text.TextUtils;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.Environment;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hanning on 4/8/15.
 */
public class HttpBaseTestCase extends BaseTestCase {

    protected Api api;
    //there are third party extension to handle conditionally skip some test programmatically.
    //but i think it is not a good idea to introduce more libs only for this purpose.
    protected boolean shouldSkipTest = false;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Environment env = new Environment();
        env.setupEnvironment(getInstrumentation().getTargetContext());

        api = new Api(getInstrumentation().getTargetContext());
        String oAuthClientId = Config.getInstance().getOAuthClientId();
        String testAccount = Config.getInstance().getTestAccountConfig().getName();
        shouldSkipTest = TextUtils.isEmpty( oAuthClientId ) || TextUtils.isEmpty(testAccount);


    }

}
