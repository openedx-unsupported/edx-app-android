package org.edx.mobile.social.facebook;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;

public class UserRequest {
    private static final String ME_ENDPOINT = "/me";

    public static void makeUserRequest(GraphRequest.Callback callback) {
        final Bundle params = new Bundle();
        params.putString("fields", "name,id,email");
        final GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                ME_ENDPOINT,
                params,
                HttpMethod.GET,
                callback
        );
        request.executeAsync();
    }
}
