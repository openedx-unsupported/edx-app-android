package org.edx.mobile.test;

import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.notification.EdxLocalParseChannel;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.notification.NotificationPreference;
import org.edx.mobile.module.notification.UserNotificationManager;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class NotificationTest extends BaseTestCase {

    private static NotificationDelegate tracker;
    private static UserNotificationManager manager;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initDelegation();
    }

    private void initDelegation() {

        if (manager == null) {
            tracker = Mockito.mock(NotificationDelegate.class);

            manager =   UserNotificationManager.getInstance();
            // use mocked tracker
            manager.setDelegate(tracker);
        }
    }

    /**
     * nothing interesting here.
     * @throws Exception
     */
    public void testNotificationManager() throws Exception {
        final String  courseId1 = "course_id_1";
        final String  channelId1 = "channel_id_1";
        manager.changeNotificationSetting(courseId1, channelId1, false);
        manager.checkCourseEnrollment(new ArrayList<EnrolledCoursesResponse>());
        manager.isSubscribedByCourseId(courseId1);
        manager.resubscribeAll( );
        manager.subscribeAndUnsubscribeToServer(channelId1,true);
        manager.unsubscribeAll();
        manager.syncWithServer();
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        print("Finished : " + getClass().getName());
    }
}

