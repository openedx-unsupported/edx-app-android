package org.edx.mobile.http;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.registration.model.RegistrationDescription;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO - we won't need this api when we fully migrate the code to okhttp
 */
public interface IApi {
    List<EnrolledCoursesResponse> getEnrolledCourses()
            throws Exception;

    EnrolledCoursesResponse getCourseById(String courseId);

    List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception;

    HandoutModel getHandout(String url, boolean fetchFromCache) throws Exception;

    List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache)
            throws Exception;


    String downloadTranscript(String url)
            throws Exception;

    SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(String courseId,
                                                                  String lastVisitedModuleId) throws Exception;

    SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception;

    RegistrationDescription getRegistrationDescription() throws Exception;

    Boolean enrollInACourse(String courseId, boolean email_opt_in) throws Exception;

    List<HttpCookie> getSessionExchangeCookie() throws Exception;

    @Deprecated
    VideoResponseModel getVideoById(String courseId, String videoId)
            throws Exception;

    @Deprecated
    Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache) throws Exception;

    @Deprecated
    ArrayList<SectionItemInterface> getLiveOrganizedVideosByChapter
            (String courseId, String chapter);

    HttpManager.HttpResult getCourseStructure(HttpRequestDelegate delegate) throws Exception;
}
