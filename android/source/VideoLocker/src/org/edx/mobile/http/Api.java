package org.edx.mobile.http;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.cache.CacheManager;
import org.edx.mobile.http.serialization.JsonBooleanDeserializer;
import org.edx.mobile.http.serialization.ShareCourseResult;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthErrorResponse;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ChapterModel;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SectionItemModel;
import org.edx.mobile.model.api.SocialLoginResponse;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.json.CreateGroupResponse;
import org.edx.mobile.model.json.GetFriendsListResponse;
import org.edx.mobile.model.json.GetGroupMembersResponse;
import org.edx.mobile.model.json.SuccessResponse;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Api {

    private HttpManager http;
    private CacheManager cache;
    private Context context;
    protected final Logger logger = new Logger(getClass().getName());

    public Api(Context context) {
        this.context = context;
        http = new HttpManager();
        cache = new CacheManager(context);
    }

    /**
     * Resets password for the given email id.
     * @param emailId
     * @return
     * @throws Exception
     */
    public ResetPasswordResponse resetPassword(String emailId)
            throws Exception {
        Bundle headerBundle = new Bundle();
        headerBundle = setCookieHeaders(headerBundle);

        Bundle params = new Bundle();
        params.putString("email", emailId);

        String url = getBaseUrl() + "/password_reset/";

        String json = http.post(url, params, headerBundle);

        if (json == null) {
            return null;
        }
        logger.debug("Reset password response=" + json);

        // store auth token response
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_JSON, json);

        Gson gson = new GsonBuilder().create();
        ResetPasswordResponse res = gson.fromJson(json, ResetPasswordResponse.class);

        return res;
    }

    /**
     * Sets cookie headers like "X-CSRFToken" in the given bundle.
     * This method is helpful in making API calls the way website does.
     * @param headerBundle
     * @return
     * @throws Exception
     */
    private Bundle setCookieHeaders(Bundle headerBundle) throws Exception {
        Header header = getLoginResponseHeaders();
        HeaderElement[] elements = header.getElements();
        if(elements[0].getName().equalsIgnoreCase("csrftoken")){
            headerBundle.putString("Cookie", elements[0].getName()
                    +"="+elements[0].getValue());
            headerBundle.putString("X-CSRFToken", elements[0].getValue());
        }else{
            for(int i=0; i<elements.length;i++){
                HeaderElement element = elements[i];
                if(element.getName().equalsIgnoreCase("csrftoken")){
                    headerBundle.putString("Cookie", elements[0].getName()
                            +"="+elements[0].getValue());
                    headerBundle.putString("X-CSRFToken", elements[0].getValue());
                    break;
                }
            }
        }

        return headerBundle;
    }

    /**
     * Executes HTTP POST for auth call, and returns response.
     * 
     * @return
     * @throws Exception
     */
    public AuthResponse auth(String username, String password)
            throws Exception {
        Bundle p = new Bundle();
        p.putString("grant_type", "password");
        p.putString("client_id", Config.getInstance().getOAuthClientId());
        p.putString("client_secret", Config.getInstance().getOAuthClientSecret());
        p.putString("username", username);
        p.putString("password", password);

        String url = getBaseUrl() + "/oauth2/access_token/";
        String json = http.post(url, p, null);
        logger.debug("Auth response= " + json);

        // store auth token response
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_JSON, json);
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.PASSWORD);

        Gson gson = new GsonBuilder().create();
        AuthResponse res = gson.fromJson(json, AuthResponse.class);

        return res;
    }

    /**
     * Returns basic profile information of the given username.
     * @deprecated Use {@link #getProfile()} instead.
     * @param username
     * @return
     * @throws Exception
     */
    public ProfileModel getProfile(String username) throws Exception {
        Bundle p = new Bundle();
        p.putString("username", username);

        String url = getBaseUrl() + "/api/mobile/v0.5/users/" + username;
        String json = http.get(url, getAuthHeaders());

        Gson gson = new GsonBuilder().create();
        ProfileModel res = gson.fromJson(json, ProfileModel.class);
        // hold the json string as it is
        res.json = json;

        logger.debug("profile=" + json);

        return res;
    }

    /**
     * Returns user's basic profile information for current active session.
     * @return
     * @throws Exception
     */
    public ProfileModel getProfile() throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");

        String url = getBaseUrl() + "/api/mobile/v0.5/my_user_info";
        String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
        
        logger.debug("Url for getProfile: " + urlWithAppendedParams);

        String json = http.get(urlWithAppendedParams, getAuthHeaders());

        if (json == null) {
            return null;
        }
        logger.debug("GetProfile response=" + json);

        Gson gson = new GsonBuilder().create();
        ProfileModel res = gson.fromJson(json, ProfileModel.class);
        res.json = json;

        // store profile json
        if (res != null) {
            // FIXME: store the profile only from one place, right now it happens from LoginTask also.
            PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
            pref.put(PrefManager.Key.PROFILE_JSON, res.json);
        }

        // hold the json string as it is
        res.json = json;

        return res;
    }

    /**
     * Returns entire course hierarchy.
     * 
     * @param courseId
     * @return
     * @throws Exception
     */
    public Map<String, SectionEntry> getCourseHierarchy(String courseId)
            throws Exception {
        return getCourseHierarchy(courseId, false);
    }

    /**
     * Returns entire course hierarchy.
     * 
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    public Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache)
            throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        String url = getBaseUrl() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
        logger.debug("Get course heirarchy url - "+url);
        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }

        //Initializing task call
        logger.debug("Received Data from Server at : "+ DateUtil.getCurrentTimeStamp());
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
            String cname = m.getChapter().name;

            // carry this courseId with video model
            m.setCourseId(courseId);

            SectionEntry s = null;
            if (chapterMap.containsKey(cname)) {
                s = chapterMap.get(cname);
            } else {
                s = new SectionEntry();
                s.chapter = cname;
                s.isChapter = true;
                s.section_url = m.section_url;
                chapterMap.put(cname, s);
            }

            // add this video to section inside in this chapter
            ArrayList<VideoResponseModel> videos = s.sections.get(m.getSection().name);
            if (videos == null) {
                s.sections.put(m.getSection().name,
                        new ArrayList<VideoResponseModel>());
                videos = s.sections.get(m.getSection().name);
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

        logger.debug("Finished converting data at "+ DateUtil.getCurrentTimeStamp());
        return chapterMap;
    }

    /**
     * Returns lecture model for given course id, chapter name and lecture name combination.
     * @param courseId
     * @param chapterName
     * @param lectureName
     * @return
     * @throws Exception
     */
    public LectureModel getLecture(String courseId, String chapterName, String lectureName)
            throws Exception {
        Map<String, SectionEntry> map = getCourseHierarchy(courseId, true);

        for (Entry<String, SectionEntry> chapterentry : map.entrySet()) {

            // identify required chapter
            if (chapterName.equals(chapterentry.getKey())) {
                for (Entry<String, ArrayList<VideoResponseModel>> entry 
                        : chapterentry.getValue().sections.entrySet()) {

                    // identify required lecture
                    if (entry.getKey().equals(lectureName)) {
                        LectureModel m = new LectureModel();
                        m.name = entry.getKey();
                        m.videos = entry.getValue();
                        return m;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns video model for given course id and video id.
     * @param courseId
     * @param videoId
     * @return
     * @throws Exception
     */
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
     * Returns any (mostly first video always) one video {@link VideoResponseModel}
     * object from subsection identified by 
     * given module id (subsectionId).
     * @param courseId
     * @param subsectionId
     * @return
     * @throws Exception
     */
    public VideoResponseModel getSubsectionById(String courseId, String subsectionId)
            throws Exception {
        Map<String, SectionEntry> map = getCourseHierarchy(courseId, true);

        // iterate chapters
        for (Entry<String, SectionEntry> chapterentry : map.entrySet()) {
            // iterate lectures
            for (Entry<String, ArrayList<VideoResponseModel>> entry : 
                chapterentry.getValue().sections.entrySet()) {
                // iterate videos 
                for (VideoResponseModel v : entry.getValue()) {
                    // identify the subsection (module) if id matches
                    if (subsectionId.equals(v.getSection().id)) {
                        return v;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns UnitUrl for given course id and video id.
     * @param courseId
     * @param videoId
     * @return
     * @throws Exception
     */
    public String getUnitUrlByVideoById(String courseId, String videoId)
            throws Exception {
        try{
            VideoResponseModel vrm = getVideoById(courseId, videoId);
            if(vrm!=null){
                return vrm.unit_url;
            }
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * Returns enrolled courses of given user.
     * 
     * @return
     * @throws Exception
     */
    public ArrayList<EnrolledCoursesResponse> getEnrolledCourses()
            throws Exception {
        return getEnrolledCourses(false);
    }

    /**
     * Returns course identified by given id from cache, null if not course is found.
     * @param courseId
     * @return
     */
    public CourseEntry getCourseById(String courseId) {
        try {
            for (EnrolledCoursesResponse r : getEnrolledCourses(true)) {
                if (r.getCourse().getId().equals(courseId)) {
                    return r.getCourse();
                }
            }
        } catch(Exception ex) {
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
    public ArrayList<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);

        Bundle p = new Bundle();
        p.putString("format", "json");
        String url = getBaseUrl() + "/api/mobile/v0.5/users/" + pref.getCurrentUserProfile().username
                + "/course_enrollments/";
        String json = null;

        if (NetworkUtil.isConnected(context) && !fetchFromCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
            // cache the response
            cache.put(url, json);
        }

        if(json == null) {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }

        logger.debug("Url "+"enrolled_courses=" + json);

        Gson gson = new GsonBuilder().create();

        AuthErrorResponse authError = null;
        try {
            // check if auth error
            authError = gson.fromJson(json, AuthErrorResponse.class);
        } catch(Exception ex) {
            // nothing to do here
        }
        if (authError != null && authError.detail != null) {
            throw new AuthException(authError);
        }

        TypeToken<ArrayList<EnrolledCoursesResponse>> t = new TypeToken<ArrayList<EnrolledCoursesResponse>>() {
        };

        ArrayList<EnrolledCoursesResponse> list = gson.fromJson(json,
                t.getType());

        return list;
    }


    /**
     * Returns list of videos in a particular course.
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    public ArrayList<VideoResponseModel> getVideosByCourseId(String courseId, boolean preferCache)
            throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        String url = getBaseUrl() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            json = http.post(url, p, getAuthHeaders());
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
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    public HandoutModel getHandout(String url, boolean preferCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("Url "+urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
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
     * Returns course info object from the given URL.
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    public CourseInfoModel getCourseInfo(String url, boolean preferCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("Url "+urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }
        logger.debug("Response of course_about= " + json);

        Gson gson = new GsonBuilder().create();
        CourseInfoModel res = gson.fromJson(json, CourseInfoModel.class);
        return res;
    }

    /**
     * Returns list of announcements for the given course id.
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    public List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache)
            throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");
        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("url : "+urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
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
     * @return
     */
    public Bundle getAuthHeaders() {
        Bundle headers = new Bundle();
        
        // generate auth headers
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        AuthResponse auth = pref.getCurrentAuth();
        
        if (auth == null || !auth.isSuccess()) {
            // this might be a login with Facebook or Google
            String token = pref.getString(PrefManager.Key.AUTH_TOKEN_SOCIAL);
            if (token != null) {
                String cookie = pref.getString(PrefManager.Key.AUTH_TOKEN_SOCIAL_COOKIE);
                
                headers.putString("Authorization", token);
                headers.putString("Cookie", cookie);
            } else {
                logger.warn("Token cannot be null when AUTH_JSON is also null, something is WRONG!");
            }
        } else {
            headers.putString("Authorization", String.format("%s %s", auth.token_type, auth.access_token));
        }
        return headers;
    }

    /**
     * Returns Stream object from the given URL.
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    public CourseInfoModel srtStream(String url, boolean preferCache) throws Exception {
        Bundle p = new Bundle();
        p.putString("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, p);
            logger.debug("Url "+urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
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

    /**
     * Returns Transcript of a given Video.
     * 
     * @param 
     * @return TranscriptModel
     * @throws Exception
     */
    public TranscriptModel getTranscriptsOfVideo(String enrollmentId,
            String videoId) throws Exception {
        try{
            TranscriptModel transcript;
            VideoResponseModel vidModel =  getVideoById(enrollmentId, videoId);
            if(vidModel!=null){
                if(vidModel.getSummary()!=null){
                    transcript = vidModel.getSummary().getTranscripts();
                    return transcript;
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    public String downloadTranscript(String url)
            throws Exception {
        if (url != null){
            try {
                if (NetworkUtil.isConnected(this.context)) {
                    String str = http.get(url, getAuthHeaders());
                    return str;
                }
            } catch (Exception ex){
                logger.error(ex);
            }
        }
        return null;
    }

    /**
     * Returns list of videos for a particular URL.
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    public ArrayList<VideoResponseModel> getVideosByURL(String courseId, String videoUrl, boolean preferCache)
            throws Exception {
        if(videoUrl==null){
            return null;
        }
        ArrayList<VideoResponseModel> vidList = getVideosByCourseId(courseId, preferCache);
        ArrayList<VideoResponseModel> list = new ArrayList<VideoResponseModel>();
        if(vidList!=null && vidList.size()>0){
            for(VideoResponseModel vrm : vidList){
                try{
                    if(vrm.getSummary().getVideo_url().equalsIgnoreCase(videoUrl)){
                        vrm.setCourseId(courseId);
                        list.add(vrm);
                    }
                }catch(Exception e){
                    logger.error(e);
                }
            }
        }

        return list;
    }

    public List<EnrolledCoursesResponse> getFriendsCourses(String oauthToken) throws Exception {
        return getFriendsCourses(false, oauthToken);
    }

    public List<EnrolledCoursesResponse> getFriendsCourses(boolean preferCache, String oauthToken) throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");
        params.putString("oauth_token", oauthToken);

        String json;
        String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/courses/friends";
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, params);
           logger.debug(urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }
        logger.debug("get_friends_courses=" + json);

        Gson gson = new GsonBuilder().create();

        AuthErrorResponse authError = null;
        try {
            // check if auth error
            authError = gson.fromJson(json, AuthErrorResponse.class);
        } catch(Exception ex) {
            // nothing to do here
        }
        if (authError != null && authError.detail != null) {
            throw new AuthException(authError);
        }

        EnrolledCoursesResponse[] courseItems = gson.fromJson(json, EnrolledCoursesResponse[].class);

        List<EnrolledCoursesResponse> list = Arrays.asList(courseItems);

        return list;

    }

    public List<SocialMember> getFriendsInCourse(String courseId, String oauthToken) throws Exception {
        return getFriendsInCourse(false, courseId, oauthToken);
    }

    public List<SocialMember> getFriendsInCourse(boolean preferCache, String courseId, String oauthToken) throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");
        params.putString("oauth_token", oauthToken);

        String json;
        String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/friends/course/" + courseId;
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, params);
            logger.debug(urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }
        logger.debug("friends_in_course=" + json);

        GetFriendsListResponse response = new Gson().fromJson(json, GetFriendsListResponse.class);
        return response.getFriends();
    }

    public boolean inviteFriendsToGroup(long[] toInvite, long groupId, String oauthToken) throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");
        //make a csv of the array
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < toInvite.length; i++) {
            csv.append(Long.toString(toInvite[i]));
            if ((i + 1) < toInvite.length) {
                csv.append(",");
            }
        }
        params.putString("member_ids", csv.toString());
        params.putString("oauth_token", oauthToken);

        String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/groups/" + Long.toString(groupId) + "/member/";
        String json = http.post(url, params, getAuthHeaders());

        if (json == null) {
            return false;
        }
        logger.debug("invite_friends=" + json);

        SuccessResponse response = new Gson().fromJson(json, SuccessResponse.class);
        return response.isSuccess();
    }

    /**
     *  return of -1 indicates an error
     */
    public long createGroup(String name, String description, boolean privacy, long adminId, String socialToken) throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");
        params.putString("name", name);
        params.putString("description", description);
        params.putString("privacy", privacy ? "open" : "closed");
        params.putString("admin-id", Long.toString(adminId));
        params.putString("oauth_token", socialToken);

        //String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/groups/create/";
        String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/groups/";
        String json = http.post(url, params, getAuthHeaders());

        if (json == null) {
            return -1;
        }
        logger.debug("create_group=" + json);

        CreateGroupResponse response = new Gson().fromJson(json, CreateGroupResponse.class);
        return Long.valueOf(response.getId());
    }

    public boolean setUserCourseShareConsent(boolean consent) throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");
        params.putString("share_with_facebook_friends", Boolean.toString(consent));

        String url = getBaseUrl() + "/api/mobile/v0.5/settings/preferences/";
        String json = http.post(url, params, getAuthHeaders());

        if (json == null) {
            return false;
        }
        logger.debug("course_share_consent=" + json);

        Gson gson = JsonBooleanDeserializer.getCaseInsensitiveBooleanGson();

        SuccessResponse response = gson.fromJson(json, ShareCourseResult.class);
        return response.isSuccess();
    }

    public boolean getUserCourseShareConsent() throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");

        String url = getBaseUrl() + "/api/mobile/v0.5/settings/preferences/";
        String urlWithAppendedParams = HttpManager.toGetUrl(url, params);
        String json = http.get(urlWithAppendedParams, getAuthHeaders());

        if (json == null) {
            return false;
        }
        logger.debug("course_share_consent=" + json);

        Gson gson = JsonBooleanDeserializer.getCaseInsensitiveBooleanGson();

        SuccessResponse response = gson.fromJson(json, ShareCourseResult.class);
        return response.isSuccess();
    }

    public List<SocialMember> getGroupMembers(boolean preferCache, long groupId) throws Exception {
        Bundle params = new Bundle();
        params.putString("format", "json");

        String json;
        String url = getBaseUrl() + "/api/mobile/v0.5/social/facebook/groups/" + groupId + "/members";
        if (NetworkUtil.isConnected(context) && !preferCache) {
            // get data from server
            String urlWithAppendedParams = HttpManager.toGetUrl(url, params);
            logger.debug(urlWithAppendedParams);
            json = http.get(urlWithAppendedParams, getAuthHeaders());
            // cache the response
            cache.put(url, json);
        } else {
            json = cache.get(url);
        }

        if (json == null) {
            return null;
        }
        logger.debug("get_group_members=" + json);

        GetGroupMembersResponse response = new Gson().fromJson(json, GetGroupMembersResponse.class);
        return response.getMembers();
    }

    /**
     * Returns list of headers for a particular Get request.
     * @return
     * @throws Exception
     */
    public Header getLoginResponseHeaders()
            throws Exception {
        String url = getBaseUrl() + "/login";
        Header header = http.getRequestHeader(url);
        return header;
    }

    /**
     * Returns API base URL for the current project configuration (mobile3 or production).
     * @return
     */
    public String getBaseUrl() {
        return Config.getInstance().getApiHostURL();
    }

    /**
     * Returns chapter model and the subsequent sections and videos in organized manner from cache.
     * @param courseId
     * @param chapter
     * @return
     */
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
                if (v.getChapter().name.equals(chapter)) {
                    // this video is under the specified chapter

                    // sort out the section of this video
                    if (sections.containsKey(v.getSection().name)) {
                        ArrayList<VideoResponseModel> sv = sections.get(v.getSection().name);
                        if (sv == null) {
                            sv = new ArrayList<VideoResponseModel>();
                        }
                        sv.add(v);
                    } else {
                        ArrayList<VideoResponseModel> vlist = new ArrayList<VideoResponseModel>();
                        vlist.add(v);
                        sections.put(v.getSection().name, vlist);
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

    public SocialLoginResponse loginByFacebook(String accessToken) throws Exception {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.FACEBOOK);
        
        return socialLogin(accessToken, PrefManager.Value.BACKEND_FACEBOOK);
    }

    public SocialLoginResponse loginByGoogle(String accessToken) throws Exception {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.GOOGLE);
        
        return socialLogin(accessToken, PrefManager.Value.BACKEND_GOOGLE);
    }

    private SocialLoginResponse socialLogin(String accessToken, String backend) 
                                throws Exception {
        Bundle headers = new Bundle();
        headers.putString("Content-Type", "application/x-www-form-urlencoded");
        
        Bundle p = new Bundle();
        p.putString("access_token", accessToken);

        String url = getBaseUrl() + "/login_oauth_token/" + backend + "/";
        logger.debug("Url for social login: " + url);
        
        String json = http.post(url, p, headers);

        if (json == null) {
            return null;
        } 
        
        if (json.length() == 0) {
            // success gives empty response for this api call
            json = "{}";
        }
        
        logger.debug(backend + " login=" + json);

        Gson gson = new GsonBuilder().create();
        SocialLoginResponse res = gson.fromJson(json, SocialLoginResponse.class);
        // hold the json string as it is
        res.json = json;
        
        // FIXME: Should not use cookies ? 
        // store cookie into preferences for later use in further API calls
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL_COOKIE, res.cookie);

        return res;
    }

    public SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId,
            String lastVisitedModuleId) throws Exception {

        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        String username = pref.getCurrentUserProfile().username;

        String url = getBaseUrl() + "/api/mobile/v0.5/users/" + username + "/course_status_info/" + courseId;
        logger.debug("PATCH url for syncLastAccessed Subsection: " + url);

        String date = DateUtil.getModificationDate();

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

    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        String username = pref.getCurrentUserProfile().username;

        String url = getBaseUrl() + "/api/mobile/v0.5/users/" + username + "/course_status_info/" + courseId;
        logger.debug("Url of get last accessed subsection: " + url);

        String date = DateUtil.getModificationDate();

        String json = http.get(url, getAuthHeaders());

        if (json == null) {
            return null;
        }
        logger.debug("Response of get last viewed subsection.id = " + json);

        Gson gson = new GsonBuilder().create();
        SyncLastAccessedSubsectionResponse res = gson.fromJson(json, SyncLastAccessedSubsectionResponse.class);

        return res;
    }

    /**
     * Creates new account.
     * @param parameters
     * @return
     * @throws Exception
     */
    public RegisterResponse register(Bundle parameters)
            throws Exception {
        String url = getBaseUrl() + "/create_account";

        String json = http.post(url, parameters, null);

        if (json == null) {
            return null;
        }
        logger.debug("Register response= " + json);

        Gson gson = new GsonBuilder().create();
        RegisterResponse res = gson.fromJson(json, RegisterResponse.class);

        return res;
    }

    /**
     * Reads registration description from assets and return Model representation of it.
     * @return
     * @throws IOException
     */
    public RegistrationDescription getRegistrationDescription() throws Exception {
        Gson gson = new Gson();

        // check if we have a cached version of registration description
        try {
            String url = getBaseUrl() + "/user_api/v1/account/registration/";
            String json = cache.get(url);
            // TODO: let the form be rendered by JSON in assets for testing, but delete below line for prod
            json = null;
            if (json != null) {
                RegistrationDescription form = gson.fromJson(json, RegistrationDescription.class);
                logger.debug("picking up registration description (form) from cache, not from assets");
                return form;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

        // if not cached, read the in-app registration description
        InputStream in = context.getAssets().open("config/registration_form.json");
        RegistrationDescription form = gson.fromJson(new InputStreamReader(in), RegistrationDescription.class);
        logger.debug("picking up registration description (form) from assets, not from cache");
        return form;
    }

    public boolean enrollInACourse(String courseId, boolean email_opt_in) throws Exception {
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
            JSONObject resultJson = new JSONObject(json);
            if (resultJson.has("error")) {
                return false;
            }else {
                return true;
            }
        }

        return false;
    }

    public String downloadRegistrationDescription() throws Exception {
        String url = getBaseUrl() + "/user_api/v1/account/registration/";
        String json = http.get(url, null);
        cache.put(url, json);
        return json;
    }
}
