package org.edx.mobile.test.http;

import org.edx.mobile.model.api.ProfileModel;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SocialLoginTests extends HttpBaseTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetProfile() throws Exception {
        ProfileModel profile = loginAPI.getProfile();
        assertNotNull(profile);
        assertNotNull("profile.email cannot be null", profile.email);
        print("finished getProfile");
    }
}
