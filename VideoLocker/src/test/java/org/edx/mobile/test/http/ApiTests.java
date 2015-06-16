package org.edx.mobile.test.http;

import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.util.Config;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

/**
 * This class contains unit tests for API calls to server.
 *
 * if we run it in the CI of github, we can not provide the credential to
 * make the service call.
 * unless we find a way to handle it,  we will disable all the testing agaist
 * real webservice right now
 * 
 */
public class ApiTests extends HttpBaseTestCase {


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Test
    public void testSyncLastSubsection() throws Exception {
        if( shouldSkipTest ) return;

        login();

        EnrolledCoursesResponse e = api.getEnrolledCourses().get(0);
        Map<String, SectionEntry> map = api.getCourseHierarchy(e.getCourse().getId());
        Entry<String, SectionEntry> entry = map.entrySet().iterator().next();
        Entry<String, ArrayList<VideoResponseModel>> subsection = entry.getValue().sections.entrySet().iterator().next();

        String courseId = e.getCourse().getId();
        String lastVisitedModuleId = subsection.getValue().get(0).getSection().getId();

        assertNotNull(courseId);
        assertNotNull(lastVisitedModuleId);

        print(String.format("course= %s ; sub-section= %s", courseId, lastVisitedModuleId));

        // TODO: lastVisitedModuleId must be section.id (id is now available)


        SyncLastAccessedSubsectionResponse model = api.syncLastAccessedSubsection(courseId, lastVisitedModuleId);
        assertNotNull(model);
        print("sync returned: " + model.last_visited_module_id);
    }

    @Test
    public void testGetLastAccessedModule() throws Exception {
        if( shouldSkipTest ) return;

        login();

        EnrolledCoursesResponse e = api.getEnrolledCourses().get(0);

        String courseId = e.getCourse().getId();
        assertNotNull(courseId);

        print(String.format("course= %s", courseId));

        SyncLastAccessedSubsectionResponse model = api.getLastAccessedSubsection(courseId);
        assertNotNull(model);
    //  print(model.json);
    }

    @Test
    public void testResetPassword() throws Exception {
        if( shouldSkipTest ) return;

        print("test: reset password");
        ResetPasswordResponse model = api.resetPassword("user@edx.org");
        assertTrue(model != null);
        print(model.value);
        print("test: finished: reset password");
    }

    @Test
    public void testHandouts() throws Exception {
        if( shouldSkipTest ) return;

        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String handoutURL = courses.get(0).getCourse().getCourse_handouts();

        HandoutModel model = api.getHandout(handoutURL, false);
        assertTrue(model != null);
        print(model.handouts_html);
    }

    @Test
    public void testChannelId() throws Exception {
        if( shouldSkipTest ) return;

        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String subscription_id = courses.get(0).getCourse().getSubscription_id();
        //should the channelId be mandatory?
        assertTrue(subscription_id != null);
    }

    @Test
    public void testCourseStructure() throws Exception {
        if( shouldSkipTest ) return;
        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String courseId = courses.get(0).getCourse().getId();

        Map<String, SectionEntry> chapters = api.getCourseHierarchy(courseId);
        for(Entry<String, SectionEntry> entry : chapters.entrySet()) {
            print("---------------" + entry.getKey() + "---------------");
            for (Entry<String, ArrayList<VideoResponseModel>> se : entry.getValue().sections.entrySet()) {
                print("------------" + se.getKey() + "------------");
                for (VideoResponseModel v : se.getValue()) {
                    print(v.getSummary().getDisplayName());
                }
            }
        }
    }

    @Test
    public void login() throws Exception {
        if( shouldSkipTest ) return;
        Config.TestAccountConfig config  = Config.getInstance().getTestAccountConfig();

        AuthResponse res = api.auth(config.getName(), config.getPassword());
        assertNotNull(res);
        assertNotNull(res.access_token);
        assertNotNull(res.token_type);
        print(res.toString());

        ProfileModel profile = api.getProfile();
        assertNotNull(profile);
    }

    @Test
    public void testGetAnnouncement() throws Exception {
        if( shouldSkipTest ) return;

        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String updatesUrl = courses.get(0).getCourse().getCourse_updates();

        List<AnnouncementsModel> res = api.getAnnouncement(updatesUrl, false);
        assertTrue(res != null);
        for (AnnouncementsModel r : res) {
            print(r.getDate());
        }
    }

    @Test
    public void testReadRegistrationDescription() throws Exception {
        if( shouldSkipTest ) return;

        RegistrationDescription form = api.getRegistrationDescription();

        assertNotNull(form);
        assertNotNull(form.getEndpoint());
        assertNotNull(form.getMethod());
        assertNotNull(form.getFields());
        assertTrue(form.getFields().size() > 0);

        // verify if enum type is parsed
        assertNotNull(form.getFields().get(0).getFieldType());
    }

    @Test
    public void testEnrollInACourse() throws Exception {
        if( shouldSkipTest ) return;

        print("test: Enroll in a course");

        EnrolledCoursesResponse e = api.getEnrolledCourses().get(0);
        String courseId = e.getCourse().getId();
        boolean success = api.enrollInACourse(courseId, true);
        assertTrue(success);
        print("success");
        print("test: finished: reset password");
    }

}
