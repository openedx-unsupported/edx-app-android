package org.edx.mobile.http;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.model.CourseIdObject;
import org.edx.mobile.http.model.EnrollmentRequestBody;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureJsonHandler;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit.RestAdapter;

/**
 * DESIGN NOTES -
 * retrofit uses annotation approach, which can be handy for simple cases.
 there are some challenges n our case,
 1. for the same endpoint, we can return different type of objects,
 2. the cache behavior in okhttp is controlled by http header, but in our case, it is totally controlled by our client logic.
 3. in okhttp document, cache is not thread safe, so it recommend singleton pattern, on the other hand, intercept is not individual request based.
 */
@Singleton
public class RestApiManager implements IApi{
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    IEdxEnvironment environment;

    @Inject
    LoginPrefs loginPrefs;

    private final OkHttpClient oauthBasedClient;
    private final OauthRestApi oauthRestApi;
    private final OkHttpClient client;
    private final PublicRestApi restApi;
    private final Gson gson = new Gson();
    private Context context;

    @Inject
    public RestApiManager(Context context, OkHttpClient oauthBasedClient) {
        this.context = context;
        this.oauthBasedClient = oauthBasedClient;
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setClient(new Ok3Client(oauthBasedClient))
            .setEndpoint(getBaseUrl())
            .setRequestInterceptor(new OfflineRequestInterceptor(context))
            .build();
        oauthRestApi = restAdapter.create(OauthRestApi.class);

        client = OkHttpUtil.getClient(context);
        restAdapter = new RestAdapter.Builder()
            .setClient(new Ok3Client(client))
            .setEndpoint(getBaseUrl())
            .build();
        restApi = restAdapter.create(PublicRestApi.class);
    }

    public final OkHttpClient getClient(){
        return client;
    }

    public final OkHttpClient createSpeedTestClient(){
        OkHttpClient.Builder builder = OkHttpUtil.getClient(context).newBuilder();
        int timeoutMillis = context.getResources().getInteger(R.integer.speed_test_timeout_in_milliseconds);
        return builder.connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS).build();
    }

    public  String getBaseUrl() {
        return environment.getConfig().getApiHostURL();
    }


    @Override
    public ResetPasswordResponse resetPassword(String emailId) throws Exception {
        OkHttpClient client = OkHttpUtil.getClient(context);
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setClient(new Ok3Client(client))
            .setEndpoint(getBaseUrl())
            .build();
        PublicRestApi service = restAdapter.create(PublicRestApi.class);
        return service.doResetPassword(emailId);
    }

    @Override
    public AuthResponse auth(String username, String password) throws Exception {
        AuthResponse response = restApi.doLogin("password", environment.getConfig().getOAuthClientId(), username, password);
        loginPrefs.storeAuthTokenResponse(response, LoginPrefs.AuthBackend.PASSWORD);
        return response;
    }

    @Override
    public ProfileModel getProfile() throws Exception {
        ProfileModel res = oauthRestApi.getProfile();
        loginPrefs.storeUserProfile(res);
        DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE).setUserName( res.username );
        return res;
    }

    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses() throws Exception {
        return getEnrolledCourses(false);
    }

    @Override
    public EnrolledCoursesResponse getCourseById(String courseId) {
        try {
            for (EnrolledCoursesResponse r : getEnrolledCourses(true)) {
                if (r.getCourse().getId().equals(courseId)) {
                    return r;
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception {
        if (!NetworkUtil.isConnected(context)){
            return oauthRestApi.getEnrolledCourses(loginPrefs.getUsername());
        } else if (fetchFromCache) {
            return oauthRestApi.getEnrolledCourses(loginPrefs.getUsername());
        } else {
            return oauthRestApi.getEnrolledCoursesNoCache(loginPrefs.getUsername());
        }
    }

    @Override
    public HandoutModel getHandout(String url, boolean prefCache) throws Exception {

        Bundle p = new Bundle();
        p.putString("format", "json");
        String urlWithAppendedParams = OkHttpUtil.toGetUrl(url, p);
        Request.Builder builder = new Request.Builder().url(urlWithAppendedParams);
        if (NetworkUtil.isConnected(context) || !prefCache) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Request request =builder .build();

        Response response = oauthBasedClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);

        return gson.fromJson(response.body().charStream(), HandoutModel.class);

    }

    @Override
    public List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        String urlWithAppendedParams = OkHttpUtil.toGetUrl(url, p);
        Request.Builder builder = new Request.Builder().url(urlWithAppendedParams);
        if (NetworkUtil.isConnected(context) && !preferCache )  {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Request request =builder.build();

        Response response = oauthBasedClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);

        TypeToken<List<AnnouncementsModel>> t = new TypeToken<List<AnnouncementsModel>>() {
        };

        return gson.fromJson(response.body().charStream(), t.getType());
    }



    @Override
    public String downloadTranscript(String url) throws Exception {
        if (url != null){
            Request.Builder builder = new Request.Builder().url(url);
            if ( NetworkUtil.isConnected(context) )  {
                builder.cacheControl(CacheControl.FORCE_NETWORK);
            }
            Request request =builder.build();

            Response response = oauthBasedClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);

            return response.body().string();
        }
        return null;
    }

    @Override
    public AuthResponse loginByFacebook(String accessToken) throws Exception {
        return socialLogin2(accessToken, LoginPrefs.AuthBackend.FACEBOOK);
    }

    @Override
    public AuthResponse loginByGoogle(String accessToken) throws Exception {
        return socialLogin2(accessToken, LoginPrefs.AuthBackend.GOOGLE);
    }

    private AuthResponse socialLogin2(String accessToken, @NonNull LoginPrefs.AuthBackend authBackend)
        throws Exception {
        final String backend = ApiConstants.getOAuthGroupIdForAuthBackend(authBackend);
        return restApi.doExchangeAccessToken(accessToken, environment.getConfig().getOAuthClientId(), backend);
    }

    @Override
    public SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId, String lastVisitedModuleId) throws Exception {
        String date = DateUtil.getModificationDate();
        EnrollmentRequestBody.LastAccessRequestBody body = new EnrollmentRequestBody.LastAccessRequestBody();
        body.last_visited_module_id = lastVisitedModuleId;
        body.modification_date = date;
        return  oauthRestApi.syncLastAccessedSubsection(body, loginPrefs.getUsername(), courseId);

    }

    @Override
    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        return oauthRestApi.getLastAccessedSubsection(loginPrefs.getUsername(), courseId);
    }

    @Override
    public RegisterResponse register(Bundle parameters) throws Exception {

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : parameters.keySet()) {
            builder.add(key, parameters.getString(key));
        }

        String url = getBaseUrl() + ApiConstants.URL_REGISTRATION;

        Request request = new Request.Builder()
            .url(url)
            .post(builder.build())
            .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        String json = response.body().string();

        if (TextUtils.isEmpty(json)) {
            return null;
        }

        //the server side response format is not client friendly ... so..
        try {
            FormFieldMessageBody body = gson.fromJson(json, FormFieldMessageBody.class);
            if( body != null && body.size() > 0 ){
                RegisterResponse res = new RegisterResponse();
                res.setMessageBody(body);
                return res;
            }
        }catch (Exception ex){
            //normal workflow , ignore it.
        }
        RegisterResponse res = gson.fromJson(json, RegisterResponse.class);

        return res;
    }

    @Override
    public RegistrationDescription getRegistrationDescription() throws Exception {
        Gson gson = new Gson();
        InputStream in = context.getAssets().open("config/registration_form.json");
        RegistrationDescription form = gson.fromJson(new InputStreamReader(in), RegistrationDescription.class);
        logger.debug("picking up registration description (form) from assets, not from cache");
        return form;
    }

    @Override
    public Boolean enrollInACourse(String courseId, boolean email_opt_in) throws Exception {
        String enrollUrl = getBaseUrl() + "/api/enrollment/v1/enrollment";
        logger.debug("POST url for enrolling in a Course: " + enrollUrl);

        CourseIdObject idObject = new CourseIdObject();
        idObject.email_opt_in = Boolean.toString(email_opt_in);
        idObject.course_id = courseId;
        EnrollmentRequestBody body = new EnrollmentRequestBody();
        body.course_details = idObject;

        String json = oauthRestApi.enrollACourse(body);

        if (json != null && !json.isEmpty()) {
            JSONObject resultJson = new JSONObject(json);
            if (resultJson.has("error")) {
                return false;
            }else {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<HttpCookie> getSessionExchangeCookie() throws Exception {
        String url = getBaseUrl() + "/oauth2/login/";
        return OkHttpUtil.getCookies(context, url, false);
    }

    public CourseComponent getCourseStructure(String courseId, boolean preferCache) throws Exception {
        String username = URLEncoder.encode(loginPrefs.getUsername(), "UTF-8");
        String block_counts = URLEncoder.encode("video", "UTF-8");
        String requested_fields = URLEncoder.encode("graded,format,student_view_multi_device", "UTF-8");
        String student_view_data = URLEncoder.encode("video,discussion", "UTF-8");

        String response;
        if (!NetworkUtil.isConnected(context)){
            response = oauthRestApi.getCourseOutline(courseId, username, requested_fields, student_view_data, block_counts);
        } else if (preferCache) {
            response = oauthRestApi.getCourseOutline(courseId, username, requested_fields, student_view_data, block_counts);
        } else {
            response = oauthRestApi.getCourseOutlineNoCache(courseId, username, requested_fields, student_view_data, block_counts);
        }

        CourseStructureV1Model model = new CourseStructureJsonHandler().processInput(response);
        return (CourseComponent) CourseManager.normalizeCourseStructure(model, courseId);
    }


    @Override
    public VideoResponseModel getVideoById(String courseId, String videoId) throws Exception {
        return null;
    }

    @Override
    public Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache) throws Exception {
        return null;
    }

    @Override
    public ArrayList<SectionItemInterface> getLiveOrganizedVideosByChapter(String courseId, String chapter) {
        return null;
    }

    @Override
    public HttpManager.HttpResult getCourseStructure(HttpRequestDelegate delegate) throws Exception {
        return null;
    }
}
