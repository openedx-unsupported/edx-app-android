package org.edx.mobile.test;


import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.module.notification.EdxLocalParseChannel;
import org.edx.mobile.module.notification.NotificationPreference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;


public class NotificationPreferenceTest {


    private final String  courseId1 = "course_id_1";
    private final String  courseId2 = "course_id_2";
    private final String  courseId3 = "course_id_3";
    private final String  channelId1 = "channel_id_1";
    private final String  channelId2 = "channel_id_2";
    private final String  channelId3 = "channel_id_3";
    private EdxLocalParseChannel channel1;
    private EdxLocalParseChannel channel2;
    private CourseEntry courseEntry1;
    private CourseEntry courseEntry2;
    private CourseEntry courseEntry3;

    @Before
    public  void setUp() throws Exception {

        channel1 =
            new EdxLocalParseChannel( courseId1, channelId1, true);
        channel2 =
            new EdxLocalParseChannel( courseId2, channelId2, false);

        courseEntry1 = new CourseEntry();
        courseEntry1.setId( courseId1 );
        courseEntry2 = new CourseEntry();
        courseEntry2.setId( courseId2 );
        courseEntry3 = new CourseEntry();
        courseEntry3.setId( courseId3 );
    }

    @Test
    public void testAddAndGetOp() throws Exception {

        NotificationPreference pref = new NotificationPreference();
        pref.add(channel1);
        pref.add(channel2);

        assertTrue("getByChannelId", pref.getByChannelId(channelId1).getChannelId().equals(channelId1));
        assertTrue("getByCourseId", pref.getByCourseId(courseId1).getCourseId().equals(courseId1));

        assertTrue(pref.size() == 2);

    }

    @Test
    public void testFilterOp() throws Exception {

        NotificationPreference pref = new NotificationPreference();
        pref.add(channel1);
        pref.add(channel2);

        List<CourseEntry> courseEntryList = new ArrayList<>();
        courseEntryList.add(courseEntry1);
        courseEntryList.add(courseEntry3);

        // find a list of courseId which is not in the dictionary
        List<CourseEntry> newCourses = pref.filterForNewCourses(courseEntryList);

        assertTrue("filterForNewCourses", newCourses.size() == 1 );
        assertTrue("filterForNewCourses", newCourses.get(0).getId().equals(courseId3));

        //find all the course entry in saved preference which are not in the current active
        courseEntryList.clear();
        courseEntryList.add(courseEntry1);
        List<EdxLocalParseChannel> inactiveCourses = pref.filterForInactiveCourses(courseEntryList);


        assertTrue("filterForInactiveCourses", inactiveCourses.size() == 1 );

        assertTrue("filterForInactiveCourses", inactiveCourses.get(0).getCourseId().equals(courseId2));

        List<String> subscribedChannels = pref.getAllSubscribedChannels();
        assertTrue("getAllSubscribedChannels", subscribedChannels.size() == 1);
        assertTrue("getAllSubscribedChannels", subscribedChannels.get(0).equals(channelId1));

    }

    @Test
    public void testFailedOp() throws Exception {

        NotificationPreference pref = new NotificationPreference();
        EdxLocalParseChannel failedChannel1 = new EdxLocalParseChannel( courseId1, channelId1, true);
        failedChannel1.setOperationFailed(true);
        EdxLocalParseChannel failedChannel2 = new EdxLocalParseChannel( courseId1, channelId1, true);
        failedChannel2.setOperationFailed(true);
        EdxLocalParseChannel normalChannel1 = new EdxLocalParseChannel( courseId1, channelId1, true);

        pref.add(failedChannel1);
        pref.add(failedChannel2);
        pref.add(normalChannel1);

        List<EdxLocalParseChannel> result = pref.getAllFailedUpdate();
        assertTrue("getAllFailedUpdate", result.size() == 2);
    }

    @Test
    public void testMarkFailedOp() throws Exception {

        NotificationPreference pref = new NotificationPreference();
        EdxLocalParseChannel c1 = new EdxLocalParseChannel( courseId1, channelId1, true);
        EdxLocalParseChannel c2 = new EdxLocalParseChannel( courseId1, channelId1, true);
        c2.setOperationFailed(true);
        EdxLocalParseChannel c3 = new EdxLocalParseChannel( courseId1, channelId1, true);

        pref.add(c1);
        pref.add(c2);
        pref.add(c3);

        List<EdxLocalParseChannel> result = pref.getAllFailedUpdate();
        assertTrue("getAllFailedUpdate", result.size() == 1);
        pref.markAllFailedUpdate();
        result = pref.getAllFailedUpdate();
        assertTrue("markAllFailedUpdate", result.size() == 3);
    }

    @Test
    public void testRemoveOp() throws Exception {

        NotificationPreference pref = new NotificationPreference();
        pref.add(channel1);
        pref.add(channel2);

        pref.removeByChannelId(channelId3);

        assertTrue("removeByChannelId", pref.size() == 2);
        pref.removeByCourseId(courseId3);
        assertTrue( "removeByCourseId", pref.size() == 2 );
        pref.removeByChannelId(channelId1);
        assertTrue( "removeByChannelId", pref.size() == 1 );
        pref.removeByCourseId(courseId2);
        assertTrue( "removeByCourseId", pref.size() ==  0 );
    }


    @After
    public void tearDown() throws Exception {

    }
}

