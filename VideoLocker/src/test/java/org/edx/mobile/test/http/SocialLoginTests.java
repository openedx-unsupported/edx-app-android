package org.edx.mobile.test.http;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.ProfileModel;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertNotNull;

/**
 * This class contains unit tests for API calls to server.
 *
 * if we run it in the CI of github, we can not provide the credential to
 * make the service call.
 * unless we find a way to handle it,  we will disable all the testing agaist
 * real webservice right now
 *
 */
public class SocialLoginTests extends HttpBaseTestCase  {


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Test
    public void testGetProfile() throws Exception {
        if ( shouldSkipTest ) return;

        ProfileModel profile = api.getProfile();
        assertNotNull(profile);
        assertNotNull("profile.email cannot be null", profile.email);
        print("finished getProfile");
    }
}
