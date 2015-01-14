package org.edx.mobile.test;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.SocialLoginResponse;

public class SocialLoginTests extends BaseTestCase {
    
    public void testGetProfile() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());
        ProfileModel profile = api.getProfile();
        assertNotNull(profile);
        assertNotNull("profile.email cannot be null", profile.email);
        print("finished getProfile");
    }
}
