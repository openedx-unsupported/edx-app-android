package org.edx.mobile.module.serverapi;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.edx.mobile.exception.AuthException;
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
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.module.serverapi.cache.CacheManager;
import org.edx.mobile.module.serverapi.http.HttpFactory;
import org.edx.mobile.module.serverapi.http.IHttp;
import org.edx.mobile.module.serverapi.parser.IParser;
import org.edx.mobile.module.serverapi.parser.ParserFactory;
import org.edx.mobile.module.serverapi.serialization.JsonBooleanDeserializer;
import org.edx.mobile.module.serverapi.serialization.ShareCourseResult;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rohan on 2/6/15.
 */
class IApiImpl implements IApi {

    private Context mContext;
    private IHttp http = HttpFactory.getInstance();
    private IParser parser = ParserFactory.getInstance();
    private CacheManager cache;
    private static final Logger logger = new Logger(IApiImpl.class);

    public IApiImpl(Context context) {
        this.mContext = context;
        this.cache = new CacheManager(context);
    }

    @Override
    public ResetPasswordResponse doResetPassword(String email) throws Exception {
        // validate inputs
        Validate.emailAddress(email);

        // hit login endpoint and get cookies
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.loginWebLink());
        IResponse response = http.get(request);

        // now prepare another request for reset password
        request = new IRequestImpl();
        request.setEndpoint(Endpoint.resetPassword());
        request.addParameter("email", email);
        request.addHeader(IHttp.KEY_COOKIE, response.getCookies().getString(IHttp.KEY_COOKIE));
        request.addHeader(IHttp.KEY_X_CSRFTOKEN, response.getCookies().getString(IHttp.KEY_X_CSRFTOKEN));

        // execute request
        response = http.post(request);

        // validate response
        Validate.httpResponse(response.getStatusCode(), 200);

        // parse the response and return
        return parser.parseObject(response.getResponse(), ResetPasswordResponse.class);
    }

    @Override
    public AuthResponse doLogin(String username, String password) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.loginAuth());
        request.addParameter("grant_type", "password");
        request.addParameter("client_id", Config.getInstance().getOAuthClientId());
        request.addParameter("client_secret", Config.getInstance().getOAuthClientSecret());
        request.addParameter("username", username);
        request.addParameter("password", password);

        IResponse response = http.post(request);

        // validate response
        Validate.httpResponse(response.getStatusCode(), 200);

        // store auth token response
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_JSON, response.getResponse());
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.PASSWORD);

        // parse the response and return
        return parser.parseObject(response.getResponse(), AuthResponse.class);
    }

    @Override
    public SocialLoginResponse doLoginByFacebook(String accessToken) throws Exception {
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.FACEBOOK);

        return socialLogin(accessToken, PrefManager.Value.BACKEND_FACEBOOK);
    }

    @Override
    public SocialLoginResponse doLoginByGoogle(String accessToken) throws Exception {
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.GOOGLE);

        return socialLogin(accessToken, PrefManager.Value.BACKEND_GOOGLE);
    }

    private SocialLoginResponse socialLogin(String accessToken, String backend)
            throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.loginBySocial(backend));
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addParameter("access_token", accessToken);

        IResponse response = http.post(request);

        Validate.httpResponse(response.getStatusCode(), 204);

        String strResponse = response.getResponse();
        if (TextUtils.isEmpty(strResponse)) {
            // success gives empty response for this api call
            strResponse = "{}";
        }

        logger.debug(backend + " login=" + strResponse);

        SocialLoginResponse res = parser.parseObject(response.getResponse(), SocialLoginResponse.class);

        // hold the json string as it is
        res.json = strResponse;

        // FIXME: Should not use cookies ?
        // store cookie into preferences for later use in further API calls
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL_COOKIE, res.cookie);

        return res;
    }

    @Override
    public String doDownloadTranscript(String url) throws Exception {
        if (url != null){
            try {
                if (NetworkUtil.isConnected(mContext)) {
                    IRequest request = new IRequestImpl();
                    request.setEndpoint(url);
                    request.setHeaders(getAuthHeaders());
                    IResponse response = http.get(request);
                    return response.getResponse();
                }
            } catch (Exception ex){
                logger.error(ex);
            }
        }
        return null;
    }

    @Override
    public RegisterResponse doRegister(Bundle parameters) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.createAccount());
        request.setParameters(parameters);

        IResponse response = http.post(request);

        Validate.httpResponse(response.getStatusCode(), 200);

        logger.debug("Register response= " + response.getResponse());

        return parser.parseObject(response.getResponse(), RegisterResponse.class);
    }

    @Override
    public boolean doEnrollInACourse(String courseId, boolean email_opt_in) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.enroll());
        request.setHeaders(getAuthHeaders());

        JSONObject postBody = new JSONObject();
        JSONObject courseIdObject = new JSONObject();
        courseIdObject.put("email_opt_in", email_opt_in);
        courseIdObject.put("course_id", courseId);
        postBody.put("course_details", courseIdObject);

        request.setPostBody(postBody.toString());
        logger.debug("POST body for Enrolling in a course: " + postBody.toString());

        IResponse response = http.post(request);

        Validate.httpResponse(response.getStatusCode(), 200);

        String json = response.getResponse();
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

    @Override
    public String doDownloadRegistrationDescription() throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.registration());
        IResponse response = http.get(request);

        cache.put(request.getEndpoint(), response.getResponse());
        return response.getResponse();
    }

    @Override
    public SyncLastAccessedSubsectionResponse doSyncLastAccessedSubsection(String courseId, String lastVisitedModuleId) throws Exception {
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        String username = pref.getCurrentUserProfile().username;

        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.syncLastAccessedSubSection(username, courseId));

        String date = DateUtil.getModificationDate();

        JSONObject postBody = new JSONObject();
        postBody.put("last_visited_module_id", lastVisitedModuleId);
        postBody.put("modification_date", date);
        request.setPostBody(postBody.toString());
        logger.debug("PATCH body for syncLastAccessed Subsection: " + postBody.toString());

        IResponse response = http.patch(request);

        Validate.httpResponse(response.getStatusCode(), 200);

        logger.debug("Response of sync last viewed= " + response.getResponse());

        return parser.parseObject(response.getResponse(), SyncLastAccessedSubsectionResponse.class);
    }

    @Override
    public boolean doInviteFriendsToGroup(long[] toInvite, long groupId, String oauthToken) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.inviteFriendsToGroup(groupId));
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");
        //make a csv of the array
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < toInvite.length; i++) {
            csv.append(Long.toString(toInvite[i]));
            if ((i + 1) < toInvite.length) {
                csv.append(",");
            }
        }
        request.addParameter("member_ids", csv.toString());
        request.addParameter("oauth_token", oauthToken);

        IResponse response = http.post(request);

        Validate.httpResponse(response.getStatusCode(), 200);

        logger.debug("invite_friends=" + response.getResponse());

        SuccessResponse model = parser.parseObject(response.getResponse(), SuccessResponse.class);
        return model.isSuccess();
    }

    @Override
    public long doCreateGroup(String name, String description, boolean privacy, long adminId, String socialToken) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.createGroup());
        request.setHeaders(getAuthHeaders());

        request.addParameter("format", "json");
        request.addParameter("name", name);
        request.addParameter("description", description);
        request.addParameter("privacy", privacy ? "open" : "closed");
        request.addParameter("admin-id", Long.toString(adminId));
        request.addParameter("oauth_token", socialToken);

        IResponse response = http.post(request);

        Validate.httpResponse(response.getStatusCode(), 200);

        logger.debug("create_group=" + response.getResponse());

        CreateGroupResponse model = parser.parseObject(response.getResponse(), CreateGroupResponse.class);
        return Long.valueOf(model.getId());
    }

    @Override
    public ProfileModel getProfile() throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.profile());
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        IResponse response = http.get(request);

        Validate.httpResponse(response.getStatusCode(), 200);

        logger.debug("GetProfile response=" + response.getResponse());

        ProfileModel res = parser.parseObject(response.getResponse(), ProfileModel.class);
        res.json = response.getResponse();

        // store profile json
        // FIXME: store the profile only from one place, right now it happens from LoginTask also.
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.PROFILE_JSON, res.json);

        return res;
    }

    @Override
    public Map<String, SectionEntry> getCourseHierarchy(String courseId) throws Exception {
        return getCourseHierarchy(courseId, false);
    }

    @Override
    public Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.courseHierarchy(courseId));
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            // get data from server
            IResponse response = http.get(request);

            // cache the response
            cache.put(request.getEndpoint(), response.getResponse());
            json = response.getResponse();
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }

        //Initializing task call
        logger.debug("Received Data from Server at : "+ DateUtil.getCurrentTimeStamp());
        logger.debug("course_hierarchy= " + json);

        List<VideoResponseModel> list = parser.parseList(json, VideoResponseModel.class);

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

    @Override
    public LectureModel getLecture(String courseId, String chapterName, String lectureName) throws Exception {
        Map<String, SectionEntry> map = getCourseHierarchy(courseId, true);

        for (Map.Entry<String, SectionEntry> chapterentry : map.entrySet()) {

            // identify required chapter
            if (chapterName.equals(chapterentry.getKey())) {
                for (Map.Entry<String, ArrayList<VideoResponseModel>> entry
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

    @Override
    public VideoResponseModel getVideoById(String courseId, String videoId) throws Exception {
        Map<String, SectionEntry> map = getCourseHierarchy(courseId, true);

        // iterate chapters
        for (Map.Entry<String, SectionEntry> chapterentry : map.entrySet()) {
            // iterate lectures
            for (Map.Entry<String, ArrayList<VideoResponseModel>> entry :
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

    @Override
    public VideoResponseModel getSubsectionById(String courseId, String subsectionId) throws Exception {
        Map<String, SectionEntry> map = getCourseHierarchy(courseId, true);

        // iterate chapters
        for (Map.Entry<String, SectionEntry> chapterentry : map.entrySet()) {
            // iterate lectures
            for (Map.Entry<String, ArrayList<VideoResponseModel>> entry :
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

    @Override
    public String getUnitUrlByVideoById(String courseId, String videoId) {
        try {
            VideoResponseModel vrm = getVideoById(courseId, videoId);
            if(vrm!=null) {
                return vrm.unit_url;
            }
        } catch(Exception e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses() throws Exception {
        return getEnrolledCourses(false);
    }

    @Override
    public List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception {
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);

        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.enrollments(pref.getCurrentUserProfile().username));
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json = null;

        if (NetworkUtil.isConnected(mContext) && !fetchFromCache) {
            IResponse response = http.get(request);
            json = response.getResponse();

            // cache the response
            cache.put(request.getEndpoint(), json);
        }

        if(json == null) {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }

        logger.debug("Url "+"enrolled_courses=" + json);

        AuthErrorResponse authError = null;
        try {
            // check if auth error
            authError = parser.parseObject(json, AuthErrorResponse.class);
        } catch(Exception ex) {
            // nothing to do here
        }
        if (authError != null && authError.detail != null) {
            throw new AuthException(authError);
        }

        List<EnrolledCoursesResponse> list = parser.parseList(json, EnrolledCoursesResponse.class);
        return list;
    }

    @Override
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

    @Override
    public List<VideoResponseModel> getVideosByCourseId(String courseId, boolean preferCache) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.videosByCourseId(courseId));
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.post(request);
            json = response.getResponse();
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }
        logger.debug("videos_by_course=" + json);

        List<VideoResponseModel> list = parser.parseList(json, VideoResponseModel.class);

        return list;
    }

    @Override
    public HandoutModel getHandout(String url, boolean preferCache) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(url);
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.get(request);
            json = response.getResponse();
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }
        logger.debug("handout=" + json);

        HandoutModel res = parser.parseObject(json, HandoutModel.class);
        return res;
    }

    @Override
    public CourseInfoModel getCourseInfo(String url, boolean preferCache) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(url);
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.get(request);
            json = response.getResponse();
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }
        logger.debug("Response of course_about= " + json);

        CourseInfoModel res = parser.parseObject(json, CourseInfoModel.class);
        return res;
    }

    @Override
    public List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(url);
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json = null;
        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.get(request);
            json = response.getResponse();
            // cache the response
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }

        List<AnnouncementsModel> list = parser.parseList(json, AnnouncementsModel.class);
        return list;
    }

    @Override
    public CourseInfoModel getSrtStream(String url) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(url);
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        IResponse response = http.get(request);
        String json = response.getResponse();

        if (json == null) {
            return null;
        }
        logger.debug("srt stream= " + json);

        CourseInfoModel res = parser.parseObject(json, CourseInfoModel.class);
        return res;
    }

    @Override
    public TranscriptModel getTranscriptsOfVideo(String enrollmentId, String videoId) {
        try {
            TranscriptModel transcript;
            VideoResponseModel vidModel =  getVideoById(enrollmentId, videoId);
            if(vidModel!=null){
                if(vidModel.getSummary()!=null){
                    transcript = vidModel.getSummary().getTranscripts();
                    return transcript;
                }
            }
        } catch(Exception e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public List<VideoResponseModel> getVideosByURL(String courseId, String videoUrl, boolean preferCache) throws Exception {
        if(videoUrl==null){
            return null;
        }
        List<VideoResponseModel> vidList = getVideosByCourseId(courseId, preferCache);
        List<VideoResponseModel> list = new ArrayList<>();
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

    @Override
    public List<EnrolledCoursesResponse> getFriendsCourses(String oauthToken) throws Exception {
        return getFriendsCourses(false, oauthToken);
    }

    @Override
    public List<EnrolledCoursesResponse> getFriendsCourses(boolean preferCache, String oauthToken) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.friendsCourses());
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");
        request.addParameter("oauth_token", oauthToken);

        String json;

        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.get(request);
            json = response.getResponse();
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }
        logger.debug("get_friends_courses=" + json);

        AuthErrorResponse authError = null;
        try {
            // check if auth error
            authError = parser.parseObject(json, AuthErrorResponse.class);
        } catch(Exception ex) {
            // nothing to do here
        }
        if (authError != null && authError.detail != null) {
            throw new AuthException(authError);
        }

        List<EnrolledCoursesResponse> list = parser.parseList(json, EnrolledCoursesResponse.class);
        return list;
    }

    @Override
    public List<SocialMember> getFriendsInCourse(String courseId, String oauthToken) throws Exception {
        return getFriendsInCourse(false, courseId, oauthToken);
    }

    @Override
    public List<SocialMember> getFriendsInCourse(boolean preferCache, String courseId, String oauthToken) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.friendsInCourses(courseId));
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");
        request.addParameter("oauth_token", oauthToken);

        String json;

        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.get(request);
            json = response.getResponse();
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }
        logger.debug("friends_in_course=" + json);

        GetFriendsListResponse response = parser.parseObject(json, GetFriendsListResponse.class);
        return response.getFriends();
    }

    @Override
    public boolean setUserCourseShareConsent(boolean consent) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.userCourseShareConsent());
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");
        request.addParameter("share_with_facebook_friends", Boolean.toString(consent));
        IResponse response = http.post(request);
        logger.debug("course_share_consent=" + response.getResponse());
        Gson gson = JsonBooleanDeserializer.getCaseInsensitiveBooleanGson();
        ShareCourseResult r = gson.fromJson(response.getResponse(), ShareCourseResult.class);
        return r.isSuccess();
    }

    @Override
    public boolean getUserCourseShareConsent() throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.userCourseShareConsent());
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");
        IResponse response = http.get(request);
        if (response.getResponse() == null) {
            return false;
        }
        logger.debug("course_share_consent=" + response.getResponse());
        Gson gson = JsonBooleanDeserializer.getCaseInsensitiveBooleanGson();
        SuccessResponse model = gson.fromJson(response.getResponse(), ShareCourseResult.class);
        return model.isSuccess();
    }

    @Override
    public List<SocialMember> getGroupMembers(boolean preferCache, long groupId) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(Endpoint.groupMembers(String.valueOf(groupId)));
        request.setHeaders(getAuthHeaders());
        request.addParameter("format", "json");

        String json;

        if (NetworkUtil.isConnected(mContext) && !preferCache) {
            IResponse response = http.get(request);
            json = response.getResponse();
            // cache the response
            cache.put(request.getEndpoint(), json);
        } else {
            json = cache.get(request.getEndpoint());
        }

        if (json == null) {
            return null;
        }
        logger.debug("get_group_members=" + json);

        GetGroupMembersResponse response = parser.parseObject(json, GetGroupMembersResponse.class);
        return response.getMembers();
    }

    @Override
    public List<SectionItemInterface> getLiveOrganizedVideosByChapter(String courseId, String chapter) {
        ArrayList<SectionItemInterface> list = new ArrayList<SectionItemInterface>();

        // add chapter to the result
        ChapterModel c = new ChapterModel();
        c.name = chapter;
        list.add(c);

        try {
            HashMap<String, ArrayList<VideoResponseModel>> sections = new LinkedHashMap<>();

            List<VideoResponseModel> videos = getVideosByCourseId(courseId, true);
            for (VideoResponseModel v : videos) {
                // filter videos by chapter
                if (v.getChapter().name.equals(chapter)) {
                    // this video is under the specified chapter

                    // sort out the section of this video
                    if (sections.containsKey(v.getSection().name)) {
                        ArrayList<VideoResponseModel> sv = sections.get(v.getSection().name);
                        if (sv == null) {
                            sv = new ArrayList<>();
                        }
                        sv.add(v);
                    } else {
                        ArrayList<VideoResponseModel> vlist = new ArrayList<>();
                        vlist.add(v);
                        sections.put(v.getSection().name, vlist);
                    }
                }
            }

            // now add sectioned videos to the result
            for (Map.Entry<String, ArrayList<VideoResponseModel>> entry : sections.entrySet()) {
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
    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
        String username = pref.getCurrentUserProfile().username;

        IRequest request = new IRequestImpl();
        request.setHeaders(getAuthHeaders());
        request.setEndpoint(Endpoint.syncLastAccessedSubSection(username, courseId));
        IResponse response = http.get(request);

        if (response.getResponse() == null) {
            return null;
        }
        logger.debug("Response of get last viewed subsection.id = " + response.getResponse());

        SyncLastAccessedSubsectionResponse res = parser.parseObject(response.getResponse(), SyncLastAccessedSubsectionResponse.class);
        return res;
    }

    @Override
    public RegistrationDescription getRegistrationDescription() throws Exception {
        // check if we have a cached version of registration description
        try {
            // TODO: let the form be rendered by JSON in assets for testing, but delete below line for prod
//            String json = cache.get(Endpoint.registration());
//            if (json != null) {
//                RegistrationDescription form = gson.fromJson(json, RegistrationDescription.class);
//                logger.debug("picking up registration description (form) from cache, not from assets");
//                return form;
//            }
        } catch(Exception ex) {
            logger.error(ex);
        }

        // if not cached, read the in-app registration description
        InputStream in = mContext.getAssets().open("config/registration_form.json");
        RegistrationDescription form = parser.parseObject(new InputStreamReader(in), RegistrationDescription.class);
        logger.debug("picking up registration description (form) from assets, not from cache");
        return form;
    }

    /**
     * Returns "Authorization" header with current active access token.
     * This is not a network call and returns already cached data.
     * @return
     */
    public Bundle getAuthHeaders() {
        Bundle headers = new Bundle();

        // generate auth headers
        PrefManager pref = new PrefManager(mContext, PrefManager.Pref.LOGIN);
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

    @Override
    public String get(String endpoint, Bundle parameters) throws Exception {
        IRequest request = new IRequestImpl();
        request.setEndpoint(endpoint);
        request.setParameters(parameters);
        IResponse response = http.get(request);
        return response.getResponse();
    }
}
