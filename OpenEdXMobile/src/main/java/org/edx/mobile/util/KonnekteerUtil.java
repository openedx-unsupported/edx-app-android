package org.edx.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;

import java.util.HashMap;
import java.util.Map;

public class KonnekteerUtil {

    @Inject
    private static IEdxEnvironment environment;

    private static final Logger logger = new Logger(KonnekteerUtil.class.getName());

    public static final String KEY_ORG_CODE = "organizationCode";
    public static final String KEY_COURSE_KEY = "courseKey";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_API_KEY = "apiKey";
    public static final String KEY_PLATFORM = "platform";
    public static final String KEY_TOPIC_TYPE = "topicType";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_MODE = "mode";

    public static void createMobileEndpoint(final Context context) {
        if(environment.getConfig().getPushNotificationsConfig().isEnabled()) {
            StringRequest request = new StringRequest(Request.Method.POST,
                    environment.getConfig().getPushNotificationsConfig().getmKonnekteerMobileEndpoints(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // logger.debug(new String(error.networkResponse.data));
                    logger.error(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    String token = FirebaseInstanceId.getInstance().getToken();
                    String orgCode = environment.getConfig().getPushNotificationsConfig().getmKonnekteerOrgCode();
                    String apiKey = environment.getConfig().getPushNotificationsConfig().getmKonnekteerApiKey();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put(KEY_ORG_CODE, orgCode);
                    params.put(KEY_TOKEN, token);
                    params.put(KEY_API_KEY, apiKey);
                    params.put(KEY_PLATFORM, "android");
                    params.put(KEY_MODE, "prod");
                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        }
    }

    public static void subscribe(final String orgCode,
                                 final String courseKey,
                                 final Context context) {
        if (environment.getConfig().getPushNotificationsConfig().isEnabled()) {
            StringRequest request = new StringRequest(Request.Method.POST,
                    environment.getConfig().getPushNotificationsConfig().getmKonnekteerMobileEndpointsSubscribe(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // logger.debug(new String(error.networkResponse.data));
                    logger.error(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    if (courseKey != null && !courseKey.isEmpty()) {
                        params.put(KEY_COURSE_KEY, courseKey);
                        params.put(KEY_TOPIC_TYPE, "course");
                    } else if (orgCode != null && !orgCode.isEmpty()) {
                        params.put(KEY_ORG_CODE, orgCode);
                        params.put(KEY_TOPIC_TYPE, "organization");
                    }

                    String token = FirebaseInstanceId.getInstance().getToken();
                    String apiKey = environment.getConfig().getPushNotificationsConfig().getmKonnekteerApiKey();
                    String username = environment.getLoginPrefs().getUsername();
                    String email = environment.getLoginPrefs().getLastAuthenticatedEmail();

                    params.put(KEY_TOKEN, token);
                    params.put(KEY_API_KEY, apiKey);
                    params.put(KEY_USERNAME, username);
                    params.put(KEY_EMAIL, email);

                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        }
    }
}