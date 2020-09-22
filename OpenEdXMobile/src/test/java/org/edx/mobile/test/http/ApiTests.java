package org.edx.mobile.test.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Injector;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseDateBlock;
import org.edx.mobile.model.course.CourseDates;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.DiscussionData;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.test.util.MockDataUtil;
import org.edx.mobile.util.DateUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import okhttp3.Request;

import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * This class contains unit tests for API calls to server.
 * <p/>
 * Note: We aren't actually calling a live server for responses
 * as we already have a mock server implemented.
 */

public class ApiTests extends HttpBaseTestCase {


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void inject(Injector injector) throws Exception {
        super.inject(injector);
        courseAPI = injector.getInstance(CourseAPI.class);
        courseService = injector.getInstance(CourseService.class);
    }

    // TODO: Debug and fix test failure
    @Ignore
    @Test
    public void testGetLastAccessedModule() throws Exception {
        login();

        EnrolledCoursesResponse e = executeStrict(courseAPI.getEnrolledCourses()).get(0);

        String courseId = e.getCourse().getId();
        assertNotNull(courseId);

        print(String.format("course= %s", courseId));

        SyncLastAccessedSubsectionResponse model = executeStrict(
                courseAPI.getLastAccessedSubsection(courseId));
        assertNotNull(model);
        //  print(model.json);
    }

    @Test
    public void testResetPassword() throws Exception {
        print("test: reset password");
        ResetPasswordResponse model = executeStrict(loginService.resetPassword("user@edx.org"));
        assertTrue(model != null);
        print(model.value);
        print("test: finished: reset password");
    }

    // TODO: Debug and fix test failure
    @Ignore
    @Test
    public void testHandouts() throws Exception {
        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = executeStrict(courseAPI.getEnrolledCourses());
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String handoutURL = courses.get(0).getCourse().getCourse_handouts();

        HandoutModel model = executeStrict(HandoutModel.class,
                okHttpClient.newCall(new Request.Builder()
                        .url(handoutURL)
                        .get()
                        .build()));
        assertTrue(model != null);
        print(model.handouts_html);
    }

    @Test
    public void testChannelId() throws Exception {
        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = executeStrict(courseAPI.getEnrolledCourses());
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String subscription_id = courses.get(0).getCourse().getSubscription_id();
        //should the channelId be mandatory?
        assertTrue(subscription_id != null);
    }

    @Test
    @Override
    public void login() throws Exception {
        super.login();
    }

    // TODO: Debug and fix test failure
    @Ignore
    @Test
    public void testGetAnnouncement() throws Exception {
        login();

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = executeStrict(courseAPI.getEnrolledCourses());
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String updatesUrl = courses.get(0).getCourse().getCourse_updates();

        List<AnnouncementsModel> res = executeStrict(
                new TypeToken<List<AnnouncementsModel>>() {},
                okHttpClient.newCall(new Request.Builder()
                        .url(updatesUrl)
                        .get()
                        .build()));
        assertTrue(res != null);
        for (AnnouncementsModel r : res) {
            print(r.getDate());
        }
    }

    // TODO: Debug and fix test failure
    @Ignore
    @Test
    public void testEnrollInACourse() throws Exception {
        login();

        print("test: Enroll in a course");

        EnrolledCoursesResponse e = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        String courseId = e.getCourse().getId();
        executeStrict(courseService.enrollInACourse(new CourseService.EnrollBody(courseId, true)));
        print("success");
        print("test: finished: reset password");
    }

    @Test
    public void testCourseDatesResponse() throws Exception {
        login();
        print("test: Course Dates Response");

        CourseDates dates = executeStrict(courseAPI.getCourseDates(""));
        assertNotNull(dates);
        assertNotNull(dates.getCourseDateBlocks());

        String todayDate = dates.getCourseDateBlocks().get(12).getDate();

        try (MockedStatic mockedStatic = mockStatic(DateUtil.class)) {
            mockedStatic.when(DateUtil::getCurrentTimeStamp).thenReturn(todayDate);
            Mockito.when(DateUtil.isDateToday(todayDate)).thenReturn(true);
            assertTrue(dates.isContainToday());
        }

        dates.organiseCourseDates();

        assertNotNull(dates.getCourseDatesMap());

        for (String key : dates.getCourseDatesMap().keySet()) {
            assertNotNull(key);
            ArrayList<CourseDateBlock> blocks = dates.getCourseDatesMap().get(key);
            assertNotNull(blocks);
            for (CourseDateBlock block : blocks) {
                assertNotNull(block);
            }
        }
        print("success");
        print("test: Course Dates Api working fine");
    }

    @Test
    public void testGetCourseStructure() throws Exception {
        login();

        // General overall testing of CourseComponent API without recursion
        EnrolledCoursesResponse e = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        final String courseId = e.getCourse().getId();
        final CourseStructureV1Model model = executeStrict(courseAPI.getCourseStructure(
                config.getApiUrlVersionConfig().getBlocksApiVersion(), courseId));
        final CourseComponent courseComponent = (CourseComponent) CourseAPI.normalizeCourseStructure(model, courseId);
        assertNotNull(courseComponent);
        assertNotNull(courseComponent.getRoot());
        assertEquals(courseId, courseComponent.getCourseId());

        List<IBlock> children = courseComponent.getChildren();
        assertNotNull(children);
        List<CourseComponent> childContainers = new ArrayList<>();
        List<CourseComponent> childLeafs = new ArrayList<>();
        for (IBlock c : children) {
            assertTrue(c instanceof CourseComponent);
            final CourseComponent child = (CourseComponent) c;
            assertEquals(child, courseComponent.find(new Filter<CourseComponent>() {
                @Override
                public boolean apply(CourseComponent component) {
                    return child.getId().equals(component.getId());
                }
            }));
            List<IBlock> grandchildren = child.getChildren();
            for (IBlock gc : grandchildren) {
                assertTrue(gc instanceof CourseComponent);
                final CourseComponent grandchild = (CourseComponent) c;
                assertEquals(grandchild, courseComponent.find(new Filter<CourseComponent>() {
                    @Override
                    public boolean apply(CourseComponent component) {
                        return grandchild.getId().equals(component.getId());
                    }
                }));
            }
            assertNull(child.find(new Filter<CourseComponent>() {
                @Override
                public boolean apply(CourseComponent component) {
                    return courseComponent.getId().equals(component.getId());
                }
            }));
            if (child.isContainer()) {
                childContainers.add(child);
            } else {
                childLeafs.add(child);
            }
        }
        assertEquals(childContainers, courseComponent.getChildContainers());
        assertEquals(childLeafs, courseComponent.getChildLeafs());

        assertTrue(courseComponent.isLastChild());
        int childrenSize = children.size();
        assertTrue(childrenSize > 0);
        assertTrue(((CourseComponent)
                children.get(childrenSize - 1)).isLastChild());

        BlockType blockType = courseComponent.getType();
        assertSame(courseComponent,
                courseComponent.getAncestor(Integer.MAX_VALUE));
        assertSame(courseComponent,
                courseComponent.getAncestor(EnumSet.of(blockType)));

        List<VideoBlockModel> videos = courseComponent.getVideos();
        assertNotNull(videos);
        for (HasDownloadEntry video : videos) {
            assertNotNull(video);
            assertTrue(video instanceof CourseComponent);
            CourseComponent videoComponent = (CourseComponent) video;
            assertFalse(videoComponent.isContainer());
            assertEquals(BlockType.VIDEO, videoComponent.getType());
        }

        for (BlockType type : BlockType.values()) {
            EnumSet<BlockType> typeSet = EnumSet.of(type);
            List<CourseComponent> typeComponents = new ArrayList<>();
            courseComponent.fetchAllLeafComponents(typeComponents, typeSet);
            for (CourseComponent typeComponent : typeComponents) {
                assertEquals(type, typeComponent.getType());
                verifyModelParsing(typeComponent);
            }

            if (type != blockType) {
                assertNotSame(courseComponent,
                        courseComponent.getAncestor(EnumSet.of(type)));
            }
        }

        BlockPath path = courseComponent.getPath();
        assertNotNull(path);
        assertEquals(1, path.getPath().size());
        assertSame(courseComponent, path.get(0));
        List<CourseComponent> leafComponents = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(leafComponents,
                EnumSet.allOf(BlockType.class));
        for (CourseComponent leafComponent : leafComponents) {
            BlockPath leafPath = leafComponent.getPath();
            assertNotNull(leafPath);
            int pathSize = leafPath.getPath().size();
            assertTrue(pathSize > 1);
            CourseComponent component = leafComponent;
            for (int i = pathSize - 1; i >= 0; i--) {
                assertSame(component, leafPath.get(i));
                component = component.getParent();
            }
        }


    }

    /**
     * Verifies the parsing of course structure json to {@link CourseComponent} model class.
     *
     * @param component Parsed {@link CourseComponent} model.
     */
    private void verifyModelParsing(CourseComponent component) throws IOException, JSONException {
        JSONObject jsonObj;
        jsonObj = new JSONObject(MockDataUtil.getMockResponse("get_course_structure"));
        jsonObj = jsonObj.getJSONObject("blocks");
        jsonObj = jsonObj.getJSONObject(component.getId());
        assertNotNull(jsonObj);
        // Not using the getDisplayName function below, because it returns a placeholder text
        // when the display_name field's value is empty.
        assertEquals(jsonObj.getString("display_name"), component.getInternalName());
        assertEquals(jsonObj.getBoolean("graded"), component.isGraded());
        assertEquals(jsonObj.getString("student_view_url"), component.getBlockUrl());
        assertEquals(jsonObj.getBoolean("student_view_multi_device"), component.isMultiDevice());
        assertEquals(jsonObj.getString("lms_web_url"), component.getWebUrl());

        // Type specific validations
        Gson gson = new Gson();
        switch (component.getType()) {
            case VIDEO: {
                JSONObject dataObj = jsonObj.getJSONObject("student_view_data");
                // Our current parser checks the existence of these fields to determine the type to convert into
                assertTrue(dataObj.has("encoded_videos") || dataObj.has("transcripts"));
                String dataRawJson = dataObj.toString();
                assertTrue(component instanceof VideoBlockModel);
                VideoBlockModel model = (VideoBlockModel) component;
                VideoData expected = gson.fromJson(dataRawJson, VideoData.class);
                assertEquals(expected, model.getData());
                break;
            }
            case DISCUSSION: {
                JSONObject dataObj = jsonObj.getJSONObject("student_view_data");
                // Our current parser checks the existence of these fields to determine the type to convert into
                assertTrue(dataObj.has("topic_id"));
                String dataRawJson = dataObj.toString();
                assertTrue(component instanceof DiscussionBlockModel);
                DiscussionBlockModel model = (DiscussionBlockModel) component;
                DiscussionData expected = gson.fromJson(dataRawJson, DiscussionData.class);
                assertEquals(expected, model.getData());
                break;
            }
        }
    }
}
