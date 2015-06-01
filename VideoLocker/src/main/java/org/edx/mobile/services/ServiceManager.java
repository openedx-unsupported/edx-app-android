package org.edx.mobile.services;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.http.Api;
import org.edx.mobile.http.HttpManager;
import org.edx.mobile.http.HttpRequestDelegate;
import org.edx.mobile.http.HttpRequestEndPoint;
import org.edx.mobile.http.cache.CacheManager;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureJsonHandler;
import org.edx.mobile.model.course.CourseStructureV1Model;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * This class is introduced to respect normal java application's layer architecture.
 * controller -> service -> dao -> data source
 *
 * also, api is designed in a way to make future migration to RetroFit easy
 */
public class ServiceManager {
    protected final Logger logger = new Logger(getClass().getName());

    private static ServiceManager instance;
    private final CacheManager cacheManager;
    //TODO - we will move this logic into DI framework

    private Api api;

    public static synchronized  ServiceManager getInstance(){
        if ( instance == null ){
            instance = new ServiceManager();
        }
        return instance;
    }
    public ServiceManager(){
        api = new Api(MainApplication.instance());
        cacheManager = new CacheManager(MainApplication.instance());
    }


    public HttpRequestEndPoint getEndPointCourseStructure(final String courseId){
        return new HttpRequestEndPoint() {
            public String getUrl() {
                try {
                    String block_count = URLEncoder.encode("[\"video\"]", "UTF-8");
                    String block_data = URLEncoder.encode("{\"video\":{\"profiles\":[\"mobile_high\",\"mobile_low\"],\"allow_cache_miss\":\"True\"}}", "UTF-8");

                    String url = api.getBaseUrl() + "/api/course_structure/v0/courses/" + courseId + "/blocks+navigation/?"
                        +"children=False&block_count=" + block_count + "&block_data=" + block_data;

                    logger.debug("GET url for enrolling in a Course: " + url);
                    return url;
                } catch (Exception e) {
                    logger.error(e);
                }
                return "";
            }
            public String getCacheKey() {
                return api.getBaseUrl() + "/api/mobile/v0.5/video_outlines/courses/" + courseId;
            }
            public Map<String, String> getParameters() {
                return null;
            }
        };
    }

    public CourseComponent getCourseStructureFromCache(final String courseId) throws Exception {
         return getCourseStructure(courseId, HttpRequestDelegate.REQUEST_CACHE_TYPE.ONLY_CACHE);
    }

    public CourseComponent getCourseStructure(final String courseId,
                                              HttpRequestDelegate.REQUEST_CACHE_TYPE requestCacheType) throws Exception {
        HttpRequestDelegate<CourseComponent> delegate = new HttpRequestDelegate<CourseComponent>(
                api, cacheManager, getEndPointCourseStructure(courseId)){
            @Override
            public CourseComponent fromJson(String json) throws Exception{
                CourseStructureV1Model model = new CourseStructureJsonHandler().processInput(json);
                return (CourseComponent)CourseManager.normalizeCourseStructure(model);
            }
            @Override
            public HttpManager.HttpResult invokeHttpCall() throws Exception{
                return api.getCourseStructure(this);
            }

        };

        return delegate.fetchData(requestCacheType);
    }


    public List<SectionItemInterface> getLiveOrganizedVideosByChapter
        (String courseId, final String chapter) throws Exception{
        if ( MainApplication.Q4_ASSESSMENT_FLAG ){
             CourseComponent course = this.getCourseStructureFromCache(courseId);
             if ( course == null ) {  //it means we cache the old data model in the file system
                 return api.getLiveOrganizedVideosByChapter(courseId, chapter);
             } else {
                return CourseManager.mappingAllVideoResponseModelFrom(course, new Filter<VideoResponseModel>() {
                    @Override
                    public boolean apply(VideoResponseModel videoResponseModel) {
                        return videoResponseModel != null && videoResponseModel.getChapterName().equals(chapter);
                    }
                });
             }
        } else {
            return api.getLiveOrganizedVideosByChapter(courseId, chapter);
        }
    }

    public Map<String, SectionEntry> getCourseHierarchy(String courseId)  throws Exception{
        if ( MainApplication.Q4_ASSESSMENT_FLAG ){
            CourseComponent course = this.getCourseStructureFromCache(courseId);
            if ( course == null ) {  //it means we cache the old data model in the file system
                return api.getCourseHierarchy(courseId, true);
            } else {
               return CourseManager.mappingCourseHierarchyFrom(course);
            }
        } else {
            return api.getCourseHierarchy(courseId, true);
        }
    }

    public LectureModel getLecture(String courseId, String chapterName, String chapterId, String lectureName, String lectureId)
        throws Exception {
        if ( MainApplication.Q4_ASSESSMENT_FLAG ){
            CourseComponent course = this.getCourseStructureFromCache(courseId);
            if ( course == null ) {  //it means we cache the old data model in the file system
                return api.getLecture(courseId, chapterName, lectureName);
            } else {
                return CourseManager.getLecture(course, chapterName, chapterId, lectureName, lectureId);
            }
        } else {
            return api.getLecture(courseId,chapterName,lectureName);
        }
    }

    public VideoResponseModel getVideoById(String courseId, String videoId)
        throws Exception {
        if ( MainApplication.Q4_ASSESSMENT_FLAG ){
            CourseComponent course = this.getCourseStructureFromCache(courseId);
            if ( course == null ) {  //it means we cache the old data model in the file system
                return api.getVideoById(courseId, videoId);
            } else {
                return CourseManager.getVideoById(course, videoId);
            }
        } else {
            return api.getVideoById(courseId,videoId);
        }
    }

    public String getUnitUrlByVideoById(String courseId, String videoId)
        throws Exception {
        if ( MainApplication.Q4_ASSESSMENT_FLAG ){
            CourseComponent course = this.getCourseStructureFromCache(courseId);
            if ( course == null ) {  //it means we cache the old data model in the file system
                return api.getUnitUrlByVideoById(courseId, videoId);
            } else {
                VideoResponseModel vrm = getVideoById(courseId, videoId);
                if(vrm!=null){
                    return vrm.getUnitUrl();
                } else {
                    return "";
                }
            }
        } else {
            return api.getUnitUrlByVideoById(courseId,videoId);
        }
    }

    public VideoResponseModel getSubsectionById(String courseId, String subsectionId)
        throws Exception {
        if ( MainApplication.Q4_ASSESSMENT_FLAG ){
            CourseComponent course = this.getCourseStructureFromCache(courseId);
            if ( course == null ) {  //it means we cache the old data model in the file system
                return api.getSubsectionById(courseId, subsectionId);
            } else {
                return CourseManager.getSubsectionById(course,subsectionId);
            }
        } else {
            return api.getSubsectionById(courseId,subsectionId);
        }
    }

}
