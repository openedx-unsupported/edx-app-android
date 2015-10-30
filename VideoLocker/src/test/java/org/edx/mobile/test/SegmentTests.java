package org.edx.mobile.test;

import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.ISegmentImpl;
import org.edx.mobile.module.analytics.ISegmentTracker;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

public class SegmentTests extends BaseTestCase {

    private static ISegmentTracker tracker;
    private static ISegment segment;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initTracker();
    }

    private void initTracker() {
        //Works only on physical devices, not on Emulator
        if (segment == null) {
            // create mocked instance of SegmentTracker
            tracker = Mockito.mock(ISegmentTracker.class);
            //The issue with the implemenation of Singleton for ISegment is that
            //some times the singleton is a dummy object. which causes
            // the Mocito failed here.

            // I dont want to change the implementation for now, just create ISegment
            // object directly
            segment = new ISegmentImpl( );
            // use mocked tracker
            segment.setTracker(tracker);
        }
    }


    @Test
    public void testIdentifyUser() throws Exception {
        String userID = "123";
        String email = "testEmail";
        String username = "testUsername";

        Traits traits = segment.identifyUser(userID, email, username);

        // verify that the identity method was called
        Mockito.verify(tracker).identify(Mockito.same(userID),
                Mockito.eq(traits),
                Mockito.any(Options.class));

        // verify the keys
        assertTrue(traits.containsValue(email));
        assertTrue(traits.containsValue(username));

        print(traits.toString());
    }

    @Test
    public void testVideoPlaying() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = segment.trackVideoPlaying(videoId, currentTime,
                courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.PLAYED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        testPropertiesWithCurrentTimeStamp((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoPause() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = segment.trackVideoPause(videoId, currentTime, courseId,
                unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.PAUSED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties

        testPropertiesWithCurrentTimeStamp((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoStop() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = segment.trackVideoStop(videoId, currentTime, courseId,
                unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.STOPPED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        testPropertiesWithCurrentTimeStamp((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoLoading() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        Properties props = segment.trackVideoLoading(videoId, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.LOADED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties

        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testShowTranscript() throws Exception {

        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime=10.11;
        Properties props = segment.trackShowTranscript(videoId, currentTime, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.SHOW_TRANSCRIPT),
                (Properties) Mockito.any());

        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testHideTranscript() throws Exception {
        String videoId = "testVideoId";
        double currentTime = 1000;
        String courseId = "testCourseId";
        String unitUrl = "testUnitUrl";
        Properties props = segment.trackHideTranscript(videoId, currentTime,
                courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.HIDE_TRANSCRIPT),
                (Properties) Mockito.any());

        // verify root level properties
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoSeek() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double oldTime = 10.2;
        double newTime = 10.22;
        Boolean skipSeek = true;
        Properties props = segment.trackVideoSeek(videoId, oldTime,
                newTime, courseId, unitUrl, skipSeek);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.SEEK_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        //testCommonProperties(props);
        Properties dataProps = (Properties)props.get(ISegment.Keys.DATA);
        testCommonProperties(dataProps);
        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(dataProps.containsKey(ISegment.Keys.SEEK_TYPE));
        assertTrue(dataProps.containsValue(ISegment.Values.SKIP));
        assertTrue(dataProps.containsKey(ISegment.Keys.NEW_TIME));
        assertTrue(dataProps.containsKey(ISegment.Keys.OLD_TIME));
        assertTrue(dataProps.containsKey(ISegment.Keys.REQUESTED_SKIP_INTERVAL));
        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    private void testAnalyticsContext(Properties contextProps){
        assertTrue(contextProps.containsKey(ISegment.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(ISegment.Keys.OPEN_BROWSER));
        assertTrue(contextProps.containsKey(ISegment.Keys.COMPONENT));

    }

    private void testCommonProperties(Properties props){
        assertTrue(props.containsKey(ISegment.Keys.MODULE_ID));
        assertTrue(props.containsKey(ISegment.Keys.CODE));
    }

    private void testPropertiesWithCurrentTimeStamp(Properties props){
        testCommonProperties(props);
        assertTrue(props.containsKey(ISegment.Keys.CURRENT_TIME));
    }



    @Test
    public void testDownloadComplete() throws Exception {
        String videoId = "testVideoId";
        String courseId = "testCourseId";
        String unitUrl = "testUrl";
        Properties props = segment.trackDownloadComplete(videoId, courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.VIDEO_DOWNLOADED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testOpenInBrowser() throws Exception {
        String url = "https://edx.org/";
        Properties props = segment.trackOpenInBrowser(url);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.BROWSER_LAUNCHED),
                (Properties) Mockito.any());
        // verify root level properties

        assertTrue(props.containsKey(ISegment.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        Properties data = (Properties) props.get(ISegment.Keys.DATA);
        assertTrue(data.containsKey(ISegment.Keys.TARGET_URL));

        print(props.toString());
    }

    @Test
    public void testSectionBulkVideoDownload() throws Exception {
        String enrollmentId = "testEnrollmentId";
        String section = "testSection";
        long videoCount = 10;
        Properties props = segment.trackSectionBulkVideoDownload(enrollmentId,
                section, videoCount);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.BULK_DOWNLOAD_SECTION),
                (Properties) Mockito.any());
        // verify root level properties
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties contextProps = (Properties)props.get(ISegment.Keys.CONTEXT);
        assertTrue(contextProps.containsKey(ISegment.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(ISegment.Keys.COMPONENT));

        print(props.toString());

    }
    @Test
    public void testSubSectionBulkVideoDownload() throws Exception {
        String enrollmentId = "testEnrollmentId";
        String section = "testSection";
        long videoCount = 10;
        String subSection = "testsubSection";
        Properties props = segment.trackSubSectionBulkVideoDownload(section,
                subSection, enrollmentId, videoCount);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.BULK_DOWNLOAD_SUBSECTION),(Properties) Mockito.any());

        // verify root level properties
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties contextProps = (Properties)props.get(ISegment.Keys.CONTEXT);
        assertTrue(contextProps.containsKey(ISegment.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(ISegment.Keys.COMPONENT));

        //testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());

    }

    @Test
    public void testUserLogout() throws Exception {
        Properties props= segment.trackUserLogout();
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.USER_LOGOUT),
                (Properties) Mockito.any());
        
        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));
    }

    @Test
    public void testTranscriptLanguage() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime=10.11;
        String lang="lang";
        Properties props = segment.trackTranscriptLanguage(videoId, currentTime, lang, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.LANGUAGE_CLICKED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testSingleVideoDownload() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";

        Properties props = segment.trackSingleVideoDownload(videoId, courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.SINGLE_VIDEO_DOWNLOAD),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoOrientation() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";

        double currentTime=10.20;
        boolean isLandscape=true;
        Properties props = segment.trackVideoOrientation(videoId, currentTime, isLandscape, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.SCREEN_TOGGLED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testEventLogin() throws Exception {
        String method = "Password";
        Properties props = segment.trackUserLogin(method);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.USER_LOGIN),
                (Properties) Mockito.any());
        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));
    }


    @Test
    public void testScreenView() throws Exception {
        String screenName = "testscreen";
        Properties props = segment.trackScreenView(screenName);

        // verify that the identity method was called
        Mockito.verify(tracker).screen(Mockito.anyString(),
                Mockito.eq(screenName), (Properties) Mockito.any());

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }
    @Test
    public void testtrackUserDoesNotHaveAccount() throws Exception {

        Properties props = segment.trackUserSignUpForAccount();

        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.SIGN_UP),
                (Properties) Mockito.any());
            
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }
    @Test
    public void testtrackUserFindsCourses() throws Exception {

        Properties props = segment.trackUserFindsCourses();

        // verify that the identity method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.FIND_COURSES),
                (Properties) Mockito.any());
            
        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CATEGORY));
        assertTrue(props.containsKey(ISegment.Keys.LABEL));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testTrackCreateAccountClicked() throws Exception {

        String appVersion = "Android v1.0.04";
        String source = "";
        Properties props = segment.trackCreateAccountClicked(appVersion, source);

        // verify that the identity method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Events.CREATE_ACCOUNT_CLICKED),
                (Properties) Mockito.any());

        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CATEGORY));
        assertTrue(props.containsKey(ISegment.Keys.LABEL));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testtrackEnrollClicked() throws Exception {
        String courseId = "courseId";
        boolean email_opt_in = true;
        Properties props = segment.trackEnrollClicked(courseId, email_opt_in);

        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CATEGORY));
        assertTrue(props.containsKey(ISegment.Keys.LABEL));

        Properties dataProps = (Properties)props.get(ISegment.Keys.DATA);
        assertTrue(dataProps.containsKey(ISegment.Keys.COURSE_ID));
        assertTrue(dataProps.containsKey(ISegment.Keys.EMAIL_OPT_IN));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testPushNotificationReceived() throws Exception {
        String courseId = "a_courseId";
        Properties props = segment.trackNotificationReceived(courseId);

        assertTrue("has ISegment.Keys.NAME", props.containsKey(ISegment.Keys.NAME));
        assertTrue("has ISegment.Keys.CATEGORY", props.containsKey(ISegment.Keys.CATEGORY));
        assertTrue("has ISegment.Keys.LABEL", props.containsKey(ISegment.Keys.LABEL));
        assertTrue("ISegment.Keys.NAME", props.get(ISegment.Keys.NAME).equals(ISegment.Values.NOTIFICATION_RECEIVED));
        assertTrue("ISegment.Keys.CATEGORY", props.get(ISegment.Keys.CATEGORY).equals(ISegment.Values.PUSH_NOTIFICATION));
        assertTrue("ISegment.Keys.LABEL", props.get(ISegment.Keys.LABEL).equals(courseId));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testPushNotificationTapped() throws Exception {
        String courseId = "a_courseId";
        Properties props = segment.trackNotificationTapped(courseId);

        assertTrue("has ISegment.Keys.NAME", props.containsKey(ISegment.Keys.NAME));
        assertTrue("has ISegment.Keys.CATEGORY", props.containsKey(ISegment.Keys.CATEGORY));
        assertTrue("has ISegment.Keys.LABEL", props.containsKey(ISegment.Keys.LABEL));
        assertTrue("ISegment.Keys.NAME", props.get(ISegment.Keys.NAME).equals(ISegment.Values.NOTIFICATION_TAPPED));
        assertTrue("ISegment.Keys.CATEGORY", props.get(ISegment.Keys.CATEGORY).equals(ISegment.Values.PUSH_NOTIFICATION));
        assertTrue("ISegment.Keys.LABEL", props.get(ISegment.Keys.LABEL).equals(courseId));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        print("Finished : " + getClass().getName());
    }
}

