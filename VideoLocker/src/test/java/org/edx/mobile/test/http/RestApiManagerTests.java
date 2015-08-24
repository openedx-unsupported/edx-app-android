package org.edx.mobile.test.http;

import com.google.inject.Injector;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.IApi;
import org.edx.mobile.http.RestApiManager;
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
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.util.Config;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class RestApiManagerTests extends OkHttpBaseTestCase {

    protected RestApiManager apiManager = null;
    protected ServiceManager serviceManager = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    public void addBindings() {
        super.addBindings();
        module.addBinding(IApi.class, RestApiManager.class);
    }

    @Override
    protected void inject(Injector injector ){
        super.inject(injector);
        apiManager = injector.getInstance(RestApiManager.class);
        serviceManager = injector.getInstance(ServiceManager.class);
    }

    public void testSyncLastSubsection() throws Exception {
        login();

        EnrolledCoursesResponse e = apiManager.getEnrolledCourses().get(0);
        Map<String, SectionEntry> map = serviceManager.getCourseHierarchy(e.getCourse().getId());
        Map.Entry<String, SectionEntry> entry = map.entrySet().iterator().next();
        Map.Entry<String, ArrayList<VideoResponseModel>> subsection = entry.getValue().sections.entrySet().iterator().next();

        String courseId = e.getCourse().getId();
        String lastVisitedModuleId = subsection.getValue().get(0).getSection().getId();

        assertNotNull(courseId);
        assertNotNull(lastVisitedModuleId);

        print(String.format("course= %s ; sub-section= %s", courseId, lastVisitedModuleId));

        // TODO: lastVisitedModuleId must be section.id (id is now available)


        SyncLastAccessedSubsectionResponse model = apiManager.syncLastAccessedSubsection(courseId, lastVisitedModuleId);
        assertNotNull(model);
        print("sync returned: " + model.last_visited_module_id);
    }

    public void testGetLastAccessedModule() throws Exception {

        login();

        EnrolledCoursesResponse e = apiManager.getEnrolledCourses().get(0);

        String courseId = e.getCourse().getId();
        assertNotNull(courseId);

        print(String.format("course= %s", courseId));

        SyncLastAccessedSubsectionResponse model = apiManager.getLastAccessedSubsection(courseId);
        assertNotNull(model);
        //  print(model.json);
    }

    public void testResetPassword() throws Exception {

        print("test: reset password");
        ResetPasswordResponse model = apiManager.resetPassword("user@edx.org");
        assertTrue(model != null);
        print(model.value);
        print("test: finished: reset password");
    }

    public void testHandouts() throws Exception {
         login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = apiManager.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
            courses != null && courses.size() > 0);
        String handoutURL = courses.get(0).getCourse().getCourse_handouts();

        HandoutModel model = apiManager.getHandout(handoutURL, false);
        assertTrue(model != null);
        print(model.handouts_html);
    }

    public void testChannelId() throws Exception {

        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = apiManager.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
            courses != null && courses.size() > 0);
        String subscription_id = courses.get(0).getCourse().getSubscription_id();
        //should the channelId be mandatory?
        assertTrue(subscription_id != null);
    }

    public void testCourseStructure() throws Exception {
        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = apiManager.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
            courses != null && courses.size() > 0);
        String courseId = courses.get(0).getCourse().getId();

        Map<String, SectionEntry> chapters = serviceManager.getCourseHierarchy(courseId);
        for(Map.Entry<String, SectionEntry> entry : chapters.entrySet()) {
            print("---------------" + entry.getKey() + "---------------");
            for (Map.Entry<String, ArrayList<VideoResponseModel>> se : entry.getValue().sections.entrySet()) {
                print("------------" + se.getKey() + "------------");
                for (VideoResponseModel v : se.getValue()) {
                    print(v.getSummary().getDisplayName());
                }
            }
        }
    }

    public void login() throws Exception {

        Config.TestAccountConfig config2  = config.getTestAccountConfig();

        AuthResponse res = apiManager.auth(config2.getName(), config2.getPassword());
        assertNotNull(res);
        assertNotNull(res.access_token);
        assertNotNull(res.token_type);
        print(res.toString());

        ProfileModel profile = apiManager.getProfile();
        assertNotNull(profile);
    }

    public void testGetAnnouncement() throws Exception {

        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = apiManager.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
            courses != null && courses.size() > 0);
        String updatesUrl = courses.get(0).getCourse().getCourse_updates();

        List<AnnouncementsModel> res = apiManager.getAnnouncement(updatesUrl, false);
        assertTrue(res != null);
        for (AnnouncementsModel r : res) {
            print(r.getDate());
        }
    }

    public void testReadRegistrationDescription() throws Exception {

        RegistrationDescription form = apiManager.getRegistrationDescription();

        assertNotNull(form);
        assertNotNull(form.getEndpoint());
        assertNotNull(form.getMethod());
        assertNotNull(form.getFields());
        assertTrue(form.getFields().size() > 0);

        // verify if enum type is parsed
        assertNotNull(form.getFields().get(0).getFieldType());
    }

    public void testEnrollInACourse() throws Exception {

        print("test: Enroll in a course");

        EnrolledCoursesResponse e = apiManager.getEnrolledCourses().get(0);
        String courseId = e.getCourse().getId();
        boolean success = apiManager.enrollInACourse(courseId, true);
        assertTrue(success);
        print("success");
        print("test: finished: reset password");
    }


}
