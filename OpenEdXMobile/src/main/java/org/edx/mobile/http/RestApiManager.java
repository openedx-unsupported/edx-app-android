package org.edx.mobile.http;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.model.CourseIdObject;
import org.edx.mobile.http.model.EnrollmentRequestBody;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureJsonHandler;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * DESIGN NOTES -
 * retrofit uses annotation approach, which can be handy for simple cases.
 * there are some challenges n our case,
 * 1. for the same endpoint, we can return different type of objects,
 * 2. the cache behavior in okhttp is controlled by http header, but in our case, it is totally controlled by our client logic.
 * 3. in okhttp document, cache is not thread safe, so it recommend singleton pattern, on the other hand, intercept is not individual request based.
 */
@Singleton
public class RestApiManager implements IApi {
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    IEdxEnvironment environment;

    @Inject
    LoginPrefs loginPrefs;

    private final OkHttpClient oauthBasedClient;
    private final OauthRestApi oauthRestApi;
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private Context context;

    @Inject
    public RestApiManager(Context context) {
        this.context = context;
        this.oauthBasedClient = OkHttpUtil.getOAuthBasedClientWithOfflineCache(context);
        Retrofit retrofit = new Retrofit.Builder()
                .client(oauthBasedClient)
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        oauthRestApi = retrofit.create(OauthRestApi.class);

        client = OkHttpUtil.getClient(context);
    }

    public final OkHttpClient getClient() {
        return client;
    }

    public final OkHttpClient createSpeedTestClient() {
        OkHttpClient.Builder builder = OkHttpUtil.getClient(context).newBuilder();
        int timeoutMillis = context.getResources().getInteger(R.integer.speed_test_timeout_in_milliseconds);
        return builder.connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS).build();
    }

    public String getBaseUrl() {
        return environment.getConfig().getApiHostURL();
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
        } catch (Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception {
        String orgCode = environment.getConfig().getOrganizationCode();
        if (!NetworkUtil.isConnected(context)) {
            return oauthRestApi.getEnrolledCourses(loginPrefs.getUsername(), orgCode).execute().body();
        } else if (fetchFromCache) {
            return oauthRestApi.getEnrolledCourses(loginPrefs.getUsername(), orgCode).execute().body();
        } else {
            return oauthRestApi.getEnrolledCoursesNoCache(loginPrefs.getUsername(), orgCode).execute().body();
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
        Request request = builder.build();

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
        if (NetworkUtil.isConnected(context) && !preferCache) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Request request = builder.build();

        Response response = oauthBasedClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);

        TypeToken<List<AnnouncementsModel>> t = new TypeToken<List<AnnouncementsModel>>() {
        };

        return gson.fromJson(response.body().charStream(), t.getType());
    }


    @Override
    public String downloadTranscript(String url) throws Exception {
        if (url != null) {
            Request.Builder builder = new Request.Builder().url(url);
            if (NetworkUtil.isConnected(context)) {
                builder.cacheControl(CacheControl.FORCE_NETWORK);
            }
            Request request = builder.build();

            Response response = oauthBasedClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);

            return response.body().string();
        }
        return null;
    }

    @Override
    public SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId, String lastVisitedModuleId) throws Exception {
        String date = DateUtil.getCurrentTimeStamp();
        EnrollmentRequestBody.LastAccessRequestBody body = new EnrollmentRequestBody.LastAccessRequestBody();
        body.last_visited_module_id = lastVisitedModuleId;
        body.modification_date = date;
        retrofit2.Response<SyncLastAccessedSubsectionResponse> response =
                oauthRestApi.syncLastAccessedSubsection(body, loginPrefs.getUsername(), courseId).execute();
        if (!response.isSuccessful()) {
            throw new HttpResponseStatusException(response.code());
        }
        return response.body();

    }

    @Override
    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        retrofit2.Response<SyncLastAccessedSubsectionResponse> response =
                oauthRestApi.getLastAccessedSubsection(loginPrefs.getUsername(), courseId).execute();
        if (!response.isSuccessful()) {
            throw new HttpResponseStatusException(response.code());
        }
        return response.body();
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

        retrofit2.Response<String> response = oauthRestApi.enrollACourse(body).execute();

        if (response.isSuccessful()) {
            String json = response.body();
            if (json != null && !json.isEmpty()) {
                JSONObject resultJson = new JSONObject(json);
                if (resultJson.has("error")) {
                    return false;
                } else {
                    return true;
                }
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
        if (!NetworkUtil.isConnected(context)) {
            response = oauthRestApi.getCourseOutline(courseId, username, requested_fields, student_view_data, block_counts).execute().body();
        } else if (preferCache) {
            response = oauthRestApi.getCourseOutline(courseId, username, requested_fields, student_view_data, block_counts).execute().body();
        } else {
            response = oauthRestApi.getCourseOutlineNoCache(courseId, username, requested_fields, student_view_data, block_counts).execute().body();
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
