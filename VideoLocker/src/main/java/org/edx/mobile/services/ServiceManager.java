package org.edx.mobile.services;

import android.os.Bundle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.http.HttpManager;
import org.edx.mobile.http.HttpRequestDelegate;
import org.edx.mobile.http.HttpRequestEndPoint;
import org.edx.mobile.http.IApi;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.http.cache.CacheManager;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureJsonHandler;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.social.SocialFactory;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.util.Config;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * This class is introduced to respect normal java application's layer architecture.
 * controller -> service -> dao -> data source
 * <p/>
 * also, api is designed in a way to make future migration to RetroFit easy
 * <p/>
 * UI layer should call ServiceManager, not IApi directly.
 */
@Singleton
public class ServiceManager {
    protected final Logger logger = new Logger(getClass().getName());


    private final CacheManager cacheManager;
    //TODO - we will move this logic into DI framework

    @Inject
    Config config;

    @Inject
    IApi api;

    public ServiceManager() {
        cacheManager = new CacheManager(MainApplication.instance());
    }


    public HttpRequestEndPoint getEndPointCourseStructure(final String courseId) {
        return new HttpRequestEndPoint() {
            public String getUrl() {
                try {
                    PrefManager pref = new PrefManager(MainApplication.instance(), PrefManager.Pref.LOGIN);
                    String username = URLEncoder.encode(pref.getCurrentUserProfile().username, "UTF-8");
                    String block_counts = URLEncoder.encode("video", "UTF-8");
                    String requested_fields = URLEncoder.encode("graded,format,student_view_multi_device", "UTF-8");
                    String student_view_data = URLEncoder.encode("video", "UTF-8");
                    String cId = URLEncoder.encode(courseId, "UTF-8");

                    String url = config.getApiHostURL() + "/api/courses/v1/blocks/?course_id=" + cId + "&username="
                            + username + "&depth=all&requested_fields=" + requested_fields + "&student_view_data=" + student_view_data + "&block_counts=" + block_counts + "&nav_depth=3";

                    logger.debug("GET url for enrolling in a Course: " + url);
                    return url;
                } catch (UnsupportedEncodingException e) {
                    logger.error(e);
                }
                return "";
            }

            public String getCacheKey() {
                return config.getApiHostURL() + "/api/courses/v1/blocks/?course_id=" + courseId;
            }

            public Map<String, String> getParameters() {
                return null;
            }
        };
    }

    public CourseComponent getCourseStructureFromCache(final String courseId) throws Exception {
        return getCourseStructure(courseId, OkHttpUtil.REQUEST_CACHE_TYPE.ONLY_CACHE);
    }

    public CourseComponent getCourseStructure(final String courseId,
                                              OkHttpUtil.REQUEST_CACHE_TYPE requestCacheType) throws Exception {
        HttpRequestDelegate<CourseComponent> delegate = new HttpRequestDelegate<CourseComponent>(
                api, cacheManager, getEndPointCourseStructure(courseId)) {
            @Override
            public CourseComponent fromJson(String json) throws Exception {
                CourseStructureV1Model model = new CourseStructureJsonHandler().processInput(json);
                return (CourseComponent) CourseManager.normalizeCourseStructure(model, courseId);
            }

            @Override
            public HttpManager.HttpResult invokeHttpCall() throws Exception {
                return api.getCourseStructure(this);
            }

        };

        return delegate.fetchData(requestCacheType);
    }


    public List<SectionItemInterface> getLiveOrganizedVideosByChapter(String courseId, final String chapter) throws Exception {
        CourseComponent course = this.getCourseStructureFromCache(courseId);
        if (course == null) {  //it means we cache the old data model in the file system
            return api.getLiveOrganizedVideosByChapter(courseId, chapter);
        } else {
            return CourseManager.mappingAllVideoResponseModelFrom(course, new Filter<VideoResponseModel>() {
                @Override
                public boolean apply(VideoResponseModel videoResponseModel) {
                    return videoResponseModel != null && videoResponseModel.getChapterName().equals(chapter);
                }
            });
        }
    }

    public HttpManager.HttpResult getCourseStructure(HttpRequestDelegate delegate) throws Exception {
        return null;
    }

    public Map<String, SectionEntry> getCourseHierarchy(String courseId) throws Exception {
        return getCourseHierarchy(courseId, true);
    }

    public Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean prefCache) throws Exception {
        CourseComponent course = this.getCourseStructureFromCache(courseId);
        if (course == null) {  //it means we cache the old data model in the file system
            return api.getCourseHierarchy(courseId, prefCache);
        } else {
            return CourseManager.mappingCourseHierarchyFrom(course);
        }
    }

    public VideoResponseModel getVideoById(String courseId, String videoId)
            throws Exception {
        CourseComponent course = this.getCourseStructureFromCache(courseId);
        if (course == null) {  //it means we cache the old data model in the file system
            return api.getVideoById(courseId, videoId);
        } else {
            return CourseManager.getVideoById(course, videoId);
        }
    }


    public String getUnitUrlByVideoById(String courseId, String videoId)
            throws Exception {
        CourseComponent course = this.getCourseStructureFromCache(courseId);
        if (course == null) {  //it means we cache the old data model in the file system
            return api.getUnitUrlByVideoById(courseId, videoId);
        } else {
            VideoResponseModel vrm = getVideoById(courseId, videoId);
            if (vrm != null) {
                return vrm.getUnitUrl();
            } else {
                return "";
            }
        }
    }

    public TranscriptModel getTranscriptsOfVideo(String enrollmentId,
                                                 String videoId) throws Exception {
        try {
            TranscriptModel transcript;
            VideoResponseModel vidModel = getVideoById(enrollmentId, videoId);
            if (vidModel != null) {
                if (vidModel.getSummary() != null) {
                    transcript = vidModel.getSummary().getTranscripts();
                    return transcript;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }


    public ResetPasswordResponse resetPassword(String emailId) throws Exception {
        return api.resetPassword(emailId);
    }

    public AuthResponse auth(String username, String password) throws Exception {
        return api.auth(username, password);
    }

    public ProfileModel getProfile(String username) throws Exception {
        return api.getProfile(username);
    }

    public ProfileModel getProfile() throws Exception {
        return api.getProfile();
    }

    public List<EnrolledCoursesResponse> getEnrolledCourses() throws Exception {
        return api.getEnrolledCourses();
    }

    public EnrolledCoursesResponse getCourseById(String courseId) {
        return api.getCourseById(courseId);
    }

    public List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception {
        return api.getEnrolledCourses(fetchFromCache);
    }

    public HandoutModel getHandout(String url, boolean fetchFromCache) throws Exception {
        return api.getHandout(url, fetchFromCache);
    }


    public CourseInfoModel getCourseInfo(String url, boolean preferCache) throws Exception {
        return api.getCourseInfo(url, preferCache);
    }


    public List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache) throws Exception {
        return api.getAnnouncement(url, preferCache);
    }

    public String downloadTranscript(String url) throws Exception {
        return api.downloadTranscript(url);
    }

    public List<EnrolledCoursesResponse> getFriendsCourses(String oauthToken) throws Exception {
        return api.getFriendsCourses(oauthToken);
    }


    public List<EnrolledCoursesResponse> getFriendsCourses(boolean preferCache, String oauthToken) throws Exception {
        return api.getFriendsCourses(preferCache, oauthToken);
    }


    public List<SocialMember> getFriendsInCourse(String courseId, String oauthToken) throws Exception {
        return api.getFriendsInCourse(courseId, oauthToken);
    }

    public List<SocialMember> getFriendsInCourse(boolean preferCache, String courseId, String oauthToken) throws Exception {
        return api.getFriendsInCourse(preferCache, courseId, oauthToken);
    }

    public boolean inviteFriendsToGroup(long[] toInvite, long groupId, String oauthToken) throws Exception {
        return api.inviteFriendsToGroup(toInvite, groupId, oauthToken);
    }


    public long createGroup(String name, String description, boolean privacy, long adminId, String socialToken) throws Exception {
        return api.createGroup(name, description, privacy, adminId, socialToken);
    }


    public boolean setUserCourseShareConsent(boolean consent) throws Exception {
        return api.setUserCourseShareConsent(consent);
    }


    public boolean getUserCourseShareConsent() throws Exception {
        return api.getUserCourseShareConsent();
    }


    public List<SocialMember> getGroupMembers(boolean preferCache, long groupId) throws Exception {
        return api.getGroupMembers(preferCache, groupId);
    }

    public AuthResponse socialLogin(String accessToken, SocialFactory.SOCIAL_SOURCE_TYPE socialType) throws Exception {
        return api.socialLogin(accessToken, socialType);
    }


    public AuthResponse loginByFacebook(String accessToken) throws Exception {
        return api.loginByFacebook(accessToken);
    }


    public AuthResponse loginByGoogle(String accessToken) throws Exception {
        return api.loginByGoogle(accessToken);
    }


    public SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId, String lastVisitedModuleId) throws Exception {
        return api.syncLastAccessedSubsection(courseId, lastVisitedModuleId);
    }


    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        return api.getLastAccessedSubsection(courseId);
    }

    public RegisterResponse register(Bundle parameters) throws Exception {
        return api.register(parameters);
    }


    public RegistrationDescription getRegistrationDescription() throws Exception {
        return api.getRegistrationDescription();
    }


    public Boolean enrollInACourse(String courseId, boolean email_opt_in) throws Exception {
        return api.enrollInACourse(courseId, email_opt_in);
    }

    public List<HttpCookie> getSessionExchangeCookie() throws Exception {
        return api.getSessionExchangeCookie();
    }
}
