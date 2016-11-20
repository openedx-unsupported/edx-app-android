package org.edx.mobile.test;

import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.SegmentAnalytics;
import org.edx.mobile.module.analytics.SegmentTracker;
import org.edx.mobile.module.analytics.Tracker;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

public class SegmentTests extends BaseTestCase {

    private static SegmentTracker tracker;
    private static SegmentAnalytics segment;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initTracker();
    }

    private void initTracker() {
        //Works only on physical devices, not on Emulator
        if (segment == null) {
            // create mocked instance of SegmentTracker
            tracker = Mockito.mock(SegmentTracker.class);
            //The issue with the implemenation of Singleton for ISegment is that
            //some times the singleton is a dummy object. which causes
            // the Mocito failed here.

            // I dont want to change the implementation for now, just create ISegment
            // object directly
            segment = new SegmentAnalytics( );
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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.PLAYED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        testPropertiesWithCurrentTimeStamp((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoPause() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = segment.trackVideoPause(videoId, currentTime, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.PAUSED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties

        testPropertiesWithCurrentTimeStamp((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.STOPPED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        testPropertiesWithCurrentTimeStamp((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testVideoLoading() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        Properties props = segment.trackVideoLoading(videoId, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.LOADED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties

        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.SHOW_TRANSCRIPT),
                (Properties) Mockito.any());

        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.HIDE_TRANSCRIPT),
                (Properties) Mockito.any());

        // verify root level properties
        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.SEEK_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        //testCommonProperties(props);
        Properties dataProps = (Properties)props.get(Analytics.Keys.DATA);
        testCommonProperties(dataProps);
        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(dataProps.containsKey(Analytics.Keys.SEEK_TYPE));
        assertTrue(dataProps.containsValue(Analytics.Values.SKIP));
        assertTrue(dataProps.containsKey(Analytics.Keys.NEW_TIME));
        assertTrue(dataProps.containsKey(Analytics.Keys.OLD_TIME));
        assertTrue(dataProps.containsKey(Analytics.Keys.REQUESTED_SKIP_INTERVAL));
        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());
    }

    private void testAnalyticsContext(Properties contextProps){
        assertTrue(contextProps.containsKey(Analytics.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(Analytics.Keys.OPEN_BROWSER));
        assertTrue(contextProps.containsKey(Analytics.Keys.COMPONENT));

    }

    private void testCommonProperties(Properties props){
        assertTrue(props.containsKey(Analytics.Keys.MODULE_ID));
        assertTrue(props.containsKey(Analytics.Keys.CODE));
    }

    private void testPropertiesWithCurrentTimeStamp(Properties props){
        testCommonProperties(props);
        assertTrue(props.containsKey(Analytics.Keys.CURRENT_TIME));
    }



    @Test
    public void testDownloadComplete() throws Exception {
        String videoId = "testVideoId";
        String courseId = "testCourseId";
        String unitUrl = "testUrl";
        Properties props = segment.trackDownloadComplete(videoId, courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.VIDEO_DOWNLOADED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testOpenInBrowser() throws Exception {
        String url = "https://edx.org/";
        Properties props = segment.trackOpenInBrowser(url);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.BROWSER_LAUNCHED),
                (Properties) Mockito.any());
        // verify root level properties

        assertTrue(props.containsKey(Analytics.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        Properties data = (Properties) props.get(Analytics.Keys.DATA);
        assertTrue(data.containsKey(Analytics.Keys.TARGET_URL));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.BULK_DOWNLOAD_SECTION),
                (Properties) Mockito.any());
        // verify root level properties
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        Properties contextProps = (Properties)props.get(Analytics.Keys.CONTEXT);
        assertTrue(contextProps.containsKey(Analytics.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(Analytics.Keys.COMPONENT));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.BULK_DOWNLOAD_SUBSECTION),(Properties) Mockito.any());

        // verify root level properties
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        Properties contextProps = (Properties)props.get(Analytics.Keys.CONTEXT);
        assertTrue(contextProps.containsKey(Analytics.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(Analytics.Keys.COMPONENT));

        //testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());

    }

    @Test
    public void testUserLogout() throws Exception {
        Properties props= segment.trackUserLogout();
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.USER_LOGOUT),
                (Properties) Mockito.any());

        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(props.containsKey(Analytics.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));
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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.LANGUAGE_CLICKED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testSingleVideoDownload() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";

        Properties props = segment.trackSingleVideoDownload(videoId, courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.SINGLE_VIDEO_DOWNLOAD),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

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
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.SCREEN_TOGGLED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(Analytics.Keys.DATA));
        assertTrue(props.containsKey(Analytics.Keys.NAME));

        testAnalyticsContext((Properties)props.get(Analytics.Keys.CONTEXT));

        print(props.toString());
    }

    @Test
    public void testEventLogin() throws Exception {
        String method = "Password";
        Properties props = segment.trackUserLogin(method, true);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.USER_LOGIN),
                (Properties) Mockito.any());
        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(props.containsKey(Analytics.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));
    }

    @Test
    public void testEventRegister() throws Exception {
        String method = "Password";
        Properties props = segment.trackUserRegister(method, true);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.USER_REGISTER),
                (Properties) Mockito.any());
        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(props.containsKey(Analytics.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));
    }


    @Test
    public void testScreenView() throws Exception {
        String screenName = "testscreen";
        Properties props = segment.trackScreenView(screenName, null, null, null);

        // verify that the identity method was called
        Mockito.verify(tracker).screen(Mockito.anyString(),
                Mockito.eq(screenName), (Properties) Mockito.any());

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }
    @Test
    public void testtrackUserDoesNotHaveAccount() throws Exception {

        Properties props = segment.trackUserSignUpForAccount();

        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.SIGN_UP),
                (Properties) Mockito.any());

        assertTrue(props.containsKey(Analytics.Keys.NAME));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }
    @Test
    public void testtrackUserFindsCourses() throws Exception {

        Properties props = segment.trackUserFindsCourses();

        // verify that the identity method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.FIND_COURSES),
                (Properties) Mockito.any());

        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(props.containsKey(Analytics.Keys.CATEGORY));
        assertTrue(props.containsKey(Analytics.Keys.LABEL));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testTrackCreateAccountClicked() throws Exception {

        String appVersion = "Android v1.0.04";
        String source = "";
        Properties props = segment.trackCreateAccountClicked(appVersion, source);

        // verify that the identity method was called
        Mockito.verify(tracker).track(Mockito.eq(Analytics.Events.CREATE_ACCOUNT_CLICKED),
                (Properties) Mockito.any());

        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(props.containsKey(Analytics.Keys.CATEGORY));
        assertTrue(props.containsKey(Analytics.Keys.LABEL));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testtrackEnrollClicked() throws Exception {
        String courseId = "courseId";
        boolean email_opt_in = true;
        Properties props = segment.trackEnrollClicked(courseId, email_opt_in);

        assertTrue(props.containsKey(Analytics.Keys.NAME));
        assertTrue(props.containsKey(Analytics.Keys.CATEGORY));
        assertTrue(props.containsKey(Analytics.Keys.LABEL));

        Properties dataProps = (Properties)props.get(Analytics.Keys.DATA);
        assertTrue(dataProps.containsKey(Analytics.Keys.COURSE_ID));
        assertTrue(dataProps.containsKey(Analytics.Keys.EMAIL_OPT_IN));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testPushNotificationReceived() throws Exception {
        String courseId = "a_courseId";
        Properties props = segment.trackNotificationReceived(courseId);

        assertTrue("has Analytics.Keys.NAME", props.containsKey(Analytics.Keys.NAME));
        assertTrue("has Analytics.Keys.CATEGORY", props.containsKey(Analytics.Keys.CATEGORY));
        assertTrue("has Analytics.Keys.LABEL", props.containsKey(Analytics.Keys.LABEL));
        assertTrue("Analytics.Keys.NAME", props.get(Analytics.Keys.NAME).equals(Analytics.Values.NOTIFICATION_RECEIVED));
        assertTrue("Analytics.Keys.CATEGORY", props.get(Analytics.Keys.CATEGORY).equals(Analytics.Values.PUSH_NOTIFICATION));
        assertTrue("Analytics.Keys.LABEL", props.get(Analytics.Keys.LABEL).equals(courseId));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }

    @Test
    public void testPushNotificationTapped() throws Exception {
        String courseId = "a_courseId";
        Properties props = segment.trackNotificationTapped(courseId);

        assertTrue("has Analytics.Keys.NAME", props.containsKey(Analytics.Keys.NAME));
        assertTrue("has Analytics.Keys.CATEGORY", props.containsKey(Analytics.Keys.CATEGORY));
        assertTrue("has Analytics.Keys.LABEL", props.containsKey(Analytics.Keys.LABEL));
        assertTrue("Analytics.Keys.NAME", props.get(Analytics.Keys.NAME).equals(Analytics.Values.NOTIFICATION_TAPPED));
        assertTrue("Analytics.Keys.CATEGORY", props.get(Analytics.Keys.CATEGORY).equals(Analytics.Values.PUSH_NOTIFICATION));
        assertTrue("Analytics.Keys.LABEL", props.get(Analytics.Keys.LABEL).equals(courseId));

        Properties cxt = (Properties) props.get(Analytics.Keys.CONTEXT);
        assertTrue(cxt.containsKey(Analytics.Keys.APP));

        print(props.toString());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        print("Finished : " + getClass().getName());
    }
}

