package org.edx.mobile.test;

import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.analytics.SegmentTracker;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;

public class SegmentTests extends BaseTestCase {

    private static SegmentTracker tracker;
    private static ISegment seg;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initTracker();
    }

    private void initTracker() {
        //Works only on Real devices and not on Simulators
        // mock the tracker
        if (tracker == null) {
            tracker = Mockito.mock(SegmentTracker.class);

            seg = SegmentFactory.getInstance(getInstrumentation().getTargetContext(),
                    tracker);
        }
    }


    public void testIdentifyUser() throws Exception {
        String userID = "123";
        String email = "testEmail";
        String username = "testUsername";

        Traits traits = seg.identifyUser(userID, email, username);

        // verify that the identity method was called
        Mockito.verify(tracker).identify(Mockito.same(userID), Mockito.eq(traits),
                (Options) Mockito.any());

        // verify the keys
        assertTrue(traits.containsValue(email));
        assertTrue(traits.containsValue(username));

        print(traits.toString());
    }

    public void testVideoPlaying() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = seg.trackVideoPlaying(videoId, currentTime,
                courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.PLAYED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        testPropertiesWithCurrentTimeStamp((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testVideoPause() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = seg.trackVideoPause(videoId, currentTime, courseId,
                unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.PAUSED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties

        testPropertiesWithCurrentTimeStamp((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testVideoStop() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime = 10.2;
        Properties props = seg.trackVideoStop(videoId, currentTime, courseId,
                unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.STOPPED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        testPropertiesWithCurrentTimeStamp((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testVideoLoading() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        Properties props = seg.trackVideoLoading(videoId, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.LOADED_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties

        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testShowTranscript() throws Exception {

        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime=10.11;
        Properties props = seg.trackShowTranscript(videoId, currentTime, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.SHOW_TRANSCRIPT),
                (Properties) Mockito.any());

        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testHideTranscript() throws Exception {
        String videoId = "testVideoId";
        double currentTime = 1000;
        String courseId = "testCourseId";
        String unitUrl = "testUnitUrl";
        Properties props = seg.trackHideTranscript(videoId, currentTime,
                courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.HIDE_TRANSCRIPT),
                (Properties) Mockito.any());

        // verify root level properties
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testVideoSeek() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double oldTime = 10.2;
        double newTime = 10.22;
        Properties props = seg.trackVideoSeek(videoId, oldTime,
                newTime, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.SEEK_VIDEO),
                (Properties) Mockito.any());

        // verify root level properties
        //testCommonProperties(props);
        Properties dataProps = (Properties)props.get(ISegment.Keys.DATA);
        testCommonProperties(dataProps);
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        assertTrue(dataProps.containsKey(ISegment.Keys.SEEK_TYPE));
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



    public void testDownloadComplete() throws Exception {
        String videoId = "testVideoId";
        String courseId = "testCourseId";
        String unitUrl = "testUrl";
        Properties props = seg.trackDownloadComplete(videoId, courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.VIDEO_DOWNLOADED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testOpenInBrowser() throws Exception {
        String url = "https://edx.org/";
        Properties props = seg.trackOpenInBrowser(url);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.BROWSER_LAUNCHED),
                (Properties) Mockito.any());
        // verify root level properties

        assertTrue(props.containsKey(ISegment.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        Properties data = (Properties) props.get(ISegment.Keys.DATA);
        assertTrue(data.containsKey(ISegment.Keys.TARGET_URL));

        print(props.toString());
    }
    public void testSectionBulkVideoDownload() throws Exception {
        String enrollmentId = "testEnrollmentId";
        String section = "testSection";
        long videoCount = 10;
        Properties props = seg.trackSectionBulkVideoDownload(enrollmentId,
                section, videoCount);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.BULK_DOWNLOAD_SECTION),
                (Properties) Mockito.any());
        // verify root level properties
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties contextProps = (Properties)props.get(ISegment.Keys.CONTEXT);
        assertTrue(contextProps.containsKey(ISegment.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(ISegment.Keys.COMPONENT));

        print(props.toString());

    }
    public void testSubSectionBulkVideoDownload() throws Exception {
        String enrollmentId = "testEnrollmentId";
        String section = "testSection";
        long videoCount = 10;
        String subSection = "testsubSection";
        Properties props = seg.trackSubSectionBulkVideoDownload(section,
                subSection, enrollmentId, videoCount);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.BULK_DOWNLOAD_SUBSECTION),(Properties) Mockito.any());

        // verify root level properties
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties contextProps = (Properties)props.get(ISegment.Keys.CONTEXT);
        assertTrue(contextProps.containsKey(ISegment.Keys.COURSE_ID));
        assertTrue(contextProps.containsKey(ISegment.Keys.COMPONENT));

        //testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());

    }
    
    public void testUserLogout() throws Exception {
        Properties props= seg.trackUserLogout();
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.USER_LOGOUT),
                (Properties) Mockito.any());
        
        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));
    }
    
    public void testTranscriptLanguage() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";
        double currentTime=10.11;
        String lang="lang";
        Properties props = seg.trackTranscriptLanguage(videoId, currentTime, lang, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.LANGUAGE_CLICKED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }
    
    public void testSingleVideoDownload() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";

        Properties props = seg.trackSingleVideoDownload(videoId, courseId, unitUrl);

        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.SINGLE_VIDEO_DOWNLOAD),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }
    
    public void testVideoOrientation() throws Exception {
        String videoId = "videoId";
        String courseId = "courseId";
        String unitUrl = "uniturl";

        double currentTime=10.20;
        boolean isLandscape=true;
        Properties props = seg.trackVideoOrientation(videoId, currentTime, isLandscape, courseId, unitUrl);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.SCREEN_TOGGLED),
                (Properties) Mockito.any());
        // verify root level properties
        //testCommonProperties(props);
        testCommonProperties((Properties)props.get(ISegment.Keys.DATA));
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        testAnalyticsContext((Properties)props.get(ISegment.Keys.CONTEXT));

        print(props.toString());
    }

    public void testEventLogin() throws Exception {
        String method = "Password";
        Properties props = seg.trackUserLogin(method);
        // verify that the track method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.USER_LOGIN),
                (Properties) Mockito.any());
        assertTrue(props.containsKey(ISegment.Keys.NAME));
        assertTrue(props.containsKey(ISegment.Keys.CONTEXT));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));
    }
    
    
    public void testScreenView() throws Exception {
        String screenName = "testscreen";
        Properties props = seg.screenViewsTracking(screenName);

        // verify that the identity method was called
        Mockito.verify(tracker).screen(Mockito.anyString(),
                Mockito.eq(screenName), (Properties) Mockito.any());

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }
    public void testtrackUserDoesNotHaveAccount() throws Exception {

        Properties props = seg.trackUserDoesNotHaveAccount();

        // verify that the identity method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.USER_NO_ACCOUNT),
                (Properties) Mockito.any());
            
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }
    public void testtrackUserFindsCourses() throws Exception {

        Properties props = seg.trackUserFindsCourses();

        // verify that the identity method was called
        Mockito.verify(tracker).track(Mockito.eq(ISegment.Keys.FIND_COURSES),
                (Properties) Mockito.any());
            
        assertTrue(props.containsKey(ISegment.Keys.NAME));

        Properties cxt = (Properties) props.get(ISegment.Keys.CONTEXT);
        assertTrue(cxt.containsKey(ISegment.Keys.APP));

        print(props.toString());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        print("Finished : " + getClass().getName());
    }
}

