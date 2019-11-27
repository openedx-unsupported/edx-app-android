package org.edx.mobile.social.facebook;

import androidx.annotation.NonNull;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.social.SocialMember;
import org.json.JSONException;
import org.json.JSONObject;

public class GetUserCallback {
    protected final Logger logger = new Logger(getClass().getName());

    public interface GetUserResponse {
        void onCompleted(@NonNull SocialMember socialMember);
    }

    private GetUserResponse getUserResponse;
    private GraphRequest.Callback callback;

    public GetUserCallback(final GetUserResponse getUserResponse) {
        this.getUserResponse = getUserResponse;
        callback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                SocialMember socialMember;
                try {
                    JSONObject userObj = response.getJSONObject();
                    if (userObj == null) {
                        logger.warn("Unable to get user json object from facebook graph api.");
                        return;
                    }
                    socialMember = jsonToUser(userObj);
                } catch (JSONException e) {
                    logger.error(e);
                    return;
                }
                GetUserCallback.this.getUserResponse.onCompleted(socialMember);
            }
        };
    }

    private SocialMember jsonToUser(@NonNull JSONObject user) throws JSONException {
        final String name = user.getString("name");
        final String id = user.getString("id");
        String email = null;
        if (user.has("email")) {
            email = user.getString("email");
        }
        final SocialMember member = new SocialMember(Long.parseLong(id), name);
        member.setEmail(email);
        return member;
    }

    public GraphRequest.Callback getCallback() {
        return callback;
    }
}
