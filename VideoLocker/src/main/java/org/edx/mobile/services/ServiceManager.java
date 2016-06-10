package org.edx.mobile.services;

import android.net.Uri;

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
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureJsonHandler;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.util.Config;

import java.net.HttpCookie;
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

    @Inject
    LoginPrefs loginPrefs;

    public ServiceManager() {
        cacheManager = new CacheManager(MainApplication.instance());
    }

    private HttpRequestEndPoint getEndPointCourseStructure(final String courseId) {
        return new HttpRequestEndPoint() {
            public String getUrl() {
                String url = Uri.parse(config.getApiHostURL() + "/api/courses/v1/blocks/?")
                        .buildUpon()
                        .appendQueryParameter("course_id", courseId)
                        .appendQueryParameter("username", loginPrefs.getUsername())
                        .appendQueryParameter("depth", "all")
                        .appendQueryParameter("requested_fields", "graded,format,student_view_multi_device")
                        .appendQueryParameter("student_view_data", "video,discussion")
                        .appendQueryParameter("block_counts", "video")
                        .appendQueryParameter("nav_depth", "3")
                        .toString();

                logger.debug("GET url for enrolling in a Course: " + url);
                return url;
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

    public Map<String, SectionEntry> getCourseHierarchy(String courseId) throws Exception {
        CourseComponent course = this.getCourseStructureFromCache(courseId);
        if (course == null) {  //it means we cache the old data model in the file system
            return api.getCourseHierarchy(courseId, true);
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

    public List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache) throws Exception {
        return api.getAnnouncement(url, preferCache);
    }

    public String downloadTranscript(String url) throws Exception {
        return api.downloadTranscript(url);
    }

    public SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId, String lastVisitedModuleId) throws Exception {
        return api.syncLastAccessedSubsection(courseId, lastVisitedModuleId);
    }


    public SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception {
        return api.getLastAccessedSubsection(courseId);
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
