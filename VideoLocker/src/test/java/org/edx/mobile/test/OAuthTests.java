package org.edx.mobile.test;

import android.content.Intent;
import android.os.Bundle;

import org.edx.mobile.test.http.HttpBaseTestCase;
import org.edx.mobile.view.LaunchActivity;
import org.edx.mobile.view.MyCoursesListActivity;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Early test. Not sure where to put this at the moment. Probably in a MyCoursesListActivity when
 * that gets made.
 */
public class OAuthTests extends HttpBaseTestCase {

    @Test
    @Override
    public void login() throws Exception {
        super.login();
    }

    @Test
    public void testValidOAuthToken() throws Exception {
        login();
        MyCoursesListActivity activity = Robolectric.setupActivity(MyCoursesListActivity.class);
        assertNull(Shadows.shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void testExpiredOAuthTokenRedirectsToLogin() throws Exception {
        login();

        setOAuthTokenError("token_expired");

        MyCoursesListActivity activity = Robolectric.setupActivity(MyCoursesListActivity.class);
        Intent expectedIntent = new Intent(activity, LaunchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LaunchActivity.OVERRIDE_ANIMATION_FLAG, "true");
        expectedIntent.putExtras(bundle);
        assertTrue(Shadows.shadowOf(activity).getNextStartedActivity().filterEquals(expectedIntent));
    }

}