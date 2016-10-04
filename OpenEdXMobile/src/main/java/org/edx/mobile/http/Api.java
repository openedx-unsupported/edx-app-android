package org.edx.mobile.http;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.cache.CacheManager;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthErrorResponse;
import org.edx.mobile.model.api.ChapterModel;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SectionItemModel;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Singleton
@Deprecated // Deprecated because this uses org.apache.http, which is itself deprecated
public class Api implements IApi {

    @Inject
    Config config;

    @Inject
    private HttpManager http;

    @Inject
    private CacheManager cache;

    @Inject
    private UserAPI userApi;

    @Inject
    private LoginPrefs loginPrefs;

    private Context context;
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    public Api(Context context) {
        this.context = context;

    }

    /**
     * Returns entire course hierarchy.
     *
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    @Deprecated
    public Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache)
            throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        String url = getBaseUrl() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
        logger.debug("Get course heirarchy url - " + url);
        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            json = http.get(urlWithAppendedParams, getAuthHeaders()).body;
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }

        //Initializing task call
        logger.debug("course_hierarchy= " + json);

        Gson gson = new GsonBuilder().create();
        TypeToken<ArrayList<VideoResponseModel>> t = new TypeToken<ArrayList<VideoResponseModel>>() {
        };

        ArrayList<VideoResponseModel> list = gson.fromJson(json, t.getType());

        // create hierarchy with chapters, sections and subsections
        // HashMap<String, SectionEntry> chapterMap = new HashMap<String, SectionEntry>();
        Map<String, SectionEntry> chapterMap = new LinkedHashMap<String, SectionEntry>();
        for (VideoResponseModel m : list) {
            // add each video to its corresponding chapter and section

            // add this as a chapter
            String cname = m.getChapter().getDisplayName();

            // carry this courseId with video model
            m.setCourseId(courseId);

            SectionEntry s = null;
            if (chapterMap.containsKey(cname)) {
                s = chapterMap.get(cname);
            } else {
                s = new SectionEntry();
                s.chapter = cname;
                s.isChapter = true;
                s.section_url = m.getSectionUrl();
                chapterMap.put(cname, s);
            }

            // add this video to section inside in this chapter
            ArrayList<VideoResponseModel> videos = s.sections.get(m.getSection().getDisplayName());
            if (videos == null) {
                s.sections.put(m.getSection().getDisplayName(),
                        new ArrayList<VideoResponseModel>());
                videos = s.sections.get(m.getSection().getDisplayName());
            }

            //This has been commented out because for some Videos thereare no english srt's and hence returning empty
            /*try{
            // FIXME: Utter hack code that should be removed as soon as the server starts
            // returning default english transcripts.
            if (m.getSummary().getTranscripts().englishUrl == null) {
                // Example URL: "http://mobile3.m.sandbox.edx.org/api/mobile/v0.5/video_outlines/transcripts/MITx/6.002x_4x/3T2014/Welcome/en";
                String usageKeyParts[] = m.getSummary().getId().split("/");
                String usageKey = usageKeyParts[usageKeyParts.length - 1];
                String fallbackUrl = getBaseUrl() + "/api/mobile/v0.5/video_outlines/transcripts/" + courseId + "/" + usageKey + "/en";
                m.getSummary().getTranscripts().englishUrl = fallbackUrl;
            }
            }catch(Exception e){
                logger.error(e);
            }*/
            videos.add(m);
        }

        return chapterMap;
    }

    /**
     * Returns video model for given course id and video id.
     *
     * @param courseId
     * @param videoId
     * @return
     * @throws Exception
     */
    @Deprecated
    public VideoResponseModel getVideoById(String courseId, String videoId)
            throws Exception {
        Map<String, SectionEntry> map = getCourseHierarchy(courseId, true);

        // iterate chapters
        for (Entry<String, SectionEntry> chapterentry : map.entrySet()) {
            // iterate lectures
            for (Entry<String, ArrayList<VideoResponseModel>> entry :
                    chapterentry.getValue().sections.entrySet()) {
                // iterate videos
                for (VideoResponseModel v : entry.getValue()) {

                    // identify the video
                    if (videoId.equals(v.getSummary().getId())) {
                        return v;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns enrolled courses of given user.
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses()
            throws Exception {
        return getEnrolledCourses(false);
    }

    /**
     * Returns course identified by given id from cache, null if not course is found.
     *
     * @param courseId
     * @return
     */
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

    /**
     * Returns enrolled courses of given user.
     *
     * @param fetchFromCache
     * @return
     * @throws Exception
     */
    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        p.putString("org", config.getOrganizationCode());
        String url = userApi.getUserEnrolledCoursesURL(loginPrefs.getUsername());
        String json = null;

        if (NetworkUtil.isConnected(context) && !fetchFromCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            final HttpManager.HttpResult result = http.get(urlWithAppendedParams, getAuthHeaders());
            if (result.statusCode == HttpStatus.UNAUTHORIZED) {
                throw new HttpAuthRequiredException();
            }
            json = result.body;
            // cache the response
            cache.put(url, json);
        }

        if (json == null) {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }

        logger.debug("Url " + "enrolled_courses=" + json);

        Gson gson = new GsonBuilder().create();

        AuthErrorResponse authError = null;
        try {
            // check if auth error
            authError = gson.fromJson(json, AuthErrorResponse.class);
        } catch (Exception ex) {
            // nothing to do here
        }
        if (authError != null && authError.detail != null) {
            throw new AuthException(authError.detail);
        }

        TypeToken<ArrayList<EnrolledCoursesResponse>> t = new TypeToken<ArrayList<EnrolledCoursesResponse>>() {
        };

        ArrayList<EnrolledCoursesResponse> list = gson.fromJson(json,
                t.getType());

        return list;
    }


    /**
     * Returns list of videos in a particular course.
     *
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    @Deprecated
    private ArrayList<VideoResponseModel> getVideosByCourseId(String courseId, boolean preferCache)
            throws Exception {
        Bundle p = new Bundle();
        String url = getBaseUrl() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            //Change from post to get. as post is not supported
            //FIXME -  it does not check the return code before it cache the result.
            json = http.get(url, getAuthHeaders()).body;
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }
        logger.debug("videos_by_course=" + json);

        Gson gson = new GsonBuilder().create();
        TypeToken<ArrayList<VideoResponseModel>> t = new TypeToken<ArrayList<VideoResponseModel>>() {
        };

        ArrayList<VideoResponseModel> list = gson.fromJson(json, t.getType());

        return list;
    }

    /**
     * Returns handout for the given course id.
     *
     * @param url
     * @return
     * @throws Exception
     */
    @Override
    public HandoutModel getHandout(String url, boolean prefCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(context) || !prefCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("Url " + urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders()).body;
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }
        logger.debug("handout=" + json);

        Gson gson = new GsonBuilder().create();
        HandoutModel res = gson.fromJson(json, HandoutModel.class);
        return res;
    }

    /**
     * Returns list of announcements for the given course id.
     *
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    @Override
    public List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache)
            throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("url : " + urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders()).body;
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        TypeToken<List<AnnouncementsModel>> t = new TypeToken<List<AnnouncementsModel>>() {
        };
        List<AnnouncementsModel> list = gson.fromJson(json, t.getType());

        return list;
    }


    /**
     * Returns "Authorization" header with current active access token.
     *
     * @return
     */
    private Bundle getAuthHeaders() {
        Bundle headers = new Bundle();
        final String token = loginPrefs.getAuthorizationHeader();
        headers.putString("Authorization", token);
        return headers;
    }

    /**
     * Returns Stream object from the given URL.
     *
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    private CourseInfoModel srtStream(String url, boolean preferCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("Url " + urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders()).body;
            // cache the response
            //cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }
        logger.debug("srt stream= " + json);

        Gson gson = new GsonBuilder().create();
        CourseInfoModel res = gson.fromJson(json, CourseInfoModel.class);
        return res;
    }


    @Override
    public String downloadTranscript(String url)
            throws Exception {
        if (url != null) {
            try {
                if (NetworkUtil.isConnected(this.context)) {
                    String str = http.get(url, getAuthHeaders()).body;
                    return str;
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        return null;
    }

    /**
     * Returns API base URL for the current project configuration (mobile3 or production).
     *
     * @return
     */
    public String getBaseUrl() {
        return config.getApiHostURL();
    }

    /**
     * Returns chapter model and the subsequent sections and videos in organized manner from cache.
     *
     * @param courseId
     * @param chapter
     * @return
     */
    @Deprecated
    public ArrayList<SectionItemInterface> getLiveOrganizedVideosByChapter
    (String courseId, String chapter) {

        ArrayList<SectionItemInterface> list = new ArrayList<SectionItemInterface>();

        // add chapter to the result
        ChapterModel c = new ChapterModel();
        c.name = chapter;
        list.add(c);

        try {
            HashMap<String, ArrayList<VideoResponseModel>> sections =
                    new LinkedHashMap<String, ArrayList<VideoResponseModel>>();

            ArrayList<VideoResponseModel> videos = getVideosByCourseId(courseId, true);
            for (VideoResponseModel v : videos) {
                // filter videos by chapter
                if (v.getChapter().getDisplayName().equals(chapter)) {
                    // this video is under the specified chapter

                    // sort out the section of this video
                    if (sections.containsKey(v.getSection().getDisplayName())) {
                        ArrayList<VideoResponseModel> sv = sections.get(v.getSection().getDisplayName());
                        if (sv == null) {
                            sv = new ArrayList<VideoResponseModel>();
                        }
                        sv.add(v);
                    } else {
                        ArrayList<VideoResponseModel> vlist = new ArrayList<VideoResponseModel>();
                        vlist.add(v);
                        sections.put(v.getSection().getDisplayName(), vlist);
                    }
                }
            }

            // now add sectioned videos to the result
            for (Entry<String, ArrayList<VideoResponseModel>> entry : sections.entrySet()) {
                // add section to the result
                SectionItemModel s = new SectionItemModel();
                s.name = entry.getKey();
                list.add(s);

                // add videos to the result
                if (entry.getValue() != null) {
                    for (VideoResponseModel v : entry.getValue()) {
                        list.add(v);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return list;
    }

    @Override
    public SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId,
                                                                         String lastVisitedModuleId) throws Exception {
        final String username = loginPrefs.getUsername();

        String url = getBaseUrl() + "/api/mobile/v0.5/users/" + username + "/course_status_info/" + courseId;
        logger.debug("PATCH url for syncLastAccessed Subsection: " + url);

        String date = DateUtil.getCurrentTimeStamp();

        JSONObject postBody = new JSONObject();
        postBody.put("last_visited_module_id", lastVisitedModuleId);
        postBody.put("modification_date", date);

        logger.debug("PATCH body for syncLastAccessed Subsection: " + postBody.toString());
        String json = http.post(url, postBody.toString(), getAuthHeaders(), true);

        if (json == null) {
            return null;
        }
        logger.debug("Response of sync last viewed= " + json);

        Gson gson = new GsonBuilder().create();
        SyncLastAccessedSubsectionResponse res = gson.fromJson(json, SyncLastAccessedSubsectionResponse.class);

        return res;
    }

    @Override
    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        final String username = loginPrefs.getUsername();
        String url = getBaseUrl() + "/api/mobile/v0.5/users/" + username + "/course_status_info/" + courseId;
        logger.debug("Url of get last accessed subsection: " + url);

        String json = http.get(url, getAuthHeaders()).body;

        if (json == null) {
            return null;
        }
        logger.debug("Response of get last viewed subsection.id = " + json);

        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, SyncLastAccessedSubsectionResponse.class);
    }

    /**
     * Reads registration description from assets and return Model representation of it.
     *
     * @return
     * @throws IOException
     */
    @Override
    public RegistrationDescription getRegistrationDescription() throws Exception {
        Gson gson = new Gson();
        InputStream in = context.getAssets().open("config/registration_form.json");
        RegistrationDescription form = gson.fromJson(new InputStreamReader(in), RegistrationDescription.class);
        logger.debug("picking up registration description (form) from assets, not from cache");
        return form;
    }

    @Override
    public JSONObject enrollInACourse(String courseId, boolean email_opt_in) throws Exception {
        String enrollUrl = getBaseUrl() + "/api/enrollment/v1/enrollment";
        logger.debug("POST url for enrolling in a Course: " + enrollUrl);

        JSONObject postBody = new JSONObject();
        JSONObject courseIdObject = new JSONObject();
        courseIdObject.put("email_opt_in", email_opt_in);
        courseIdObject.put("course_id", courseId);
        postBody.put("course_details", courseIdObject);

        logger.debug("POST body for Enrolling in a course: " + postBody.toString());
        String json = http.post(enrollUrl, postBody.toString(), getAuthHeaders(), false);

        if (json != null && !json.isEmpty()) {
            logger.debug("Response of Enroll in a course= " + json);
            return new JSONObject(json);
        }else{
            JSONObject resultJson = new JSONObject();
            resultJson.put("error", "No response");
            return resultJson;
        }
    }

    public String getSessionTokenExchangeUrl() {
        return getBaseUrl() + "/oauth2/login/";
    }

    /**
     * used for assessment webview, refresh session id
     */
    @Override
    public List<HttpCookie> getSessionExchangeCookie() throws Exception {
        return http.getCookies(getSessionTokenExchangeUrl(), getAuthHeaders(), false);
    }

    public HttpManager.HttpResult getCourseStructure(HttpRequestDelegate delegate) throws Exception {


        logger.debug("GET url for enrolling in a Course: " + delegate.endPoint.getUrl());

        if (NetworkUtil.isConnected(context)) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(delegate.endPoint.getUrl(), null);
            HttpManager.HttpResult result = http.get(urlWithAppendedParams, getAuthHeaders());
            return result;
        }

        return null;
    }

    public static class HttpAuthRequiredException extends Exception {
    }
}
