package org.edx.mobile.model.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.DateUtil;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * This class represents response of Authentication call to server.
 */
public class AuthResponse implements Serializable {

    public String access_token;
    public String token_type;
    public long expires_in;
    public String scope;
    public String error;
    public String refresh_token;

    // Non-API field
    public Long accessTokenExpiresAt = Long.MAX_VALUE;

    public boolean isSuccess() {
        return (error == null && access_token != null);
    }

    public static class Deserializer implements JsonDeserializer<AuthResponse> {
        private final Logger logger = new Logger(Deserializer.class.getName());

        @Override
        public AuthResponse deserialize(
                JsonElement json,
                Type typeOfT,
                JsonDeserializationContext context
        ) throws JsonParseException {
            try {
                JsonObject jsonObject = json.getAsJsonObject();
                AuthResponse authResponse = new Gson().fromJson(jsonObject, AuthResponse.class);
                authResponse.accessTokenExpiresAt = authResponse.expires_in + DateUtil.
                        getCurrentTimeInSeconds();
                return authResponse;
            } catch (Exception ex) {
                logger.error(ex, true);
                return new AuthResponse();
            }
        }
    }
}
