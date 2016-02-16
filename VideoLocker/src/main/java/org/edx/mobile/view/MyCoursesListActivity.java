package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.util.images.IntentFactory;

import java.util.ArrayList;

public class MyCoursesListActivity extends BaseSingleFragmentActivity {

    @Inject
    NotificationDelegate notificationDelegate;

    public static Intent newIntent() {
        // These flags will make it so we only have a single instance of this activity,
        // but that instance will not be restarted if it is already running
        return IntentFactory.newIntentForComponent()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureDrawer();
        setTitle(getString(R.string.label_my_courses));
        environment.getSegment().trackScreenView(getString(R.string.label_my_courses));
    }

    @Override
    public Fragment getFirstFragment() {
        return new MyCourseListTabFragment();
    }


    @Override
    protected void onResume() {
        super.onResume();
        notificationDelegate.checkAppUpgrade();
    }

    public void updateDatabaseAfterDownload(ArrayList<EnrolledCoursesResponse> list) {
        if (list != null && list.size() > 0) {
            //update all videos in the DB as Deactivated
            environment.getDatabase().updateAllVideosAsDeactivated(dataCallback);

            for (int i = 0; i < list.size(); i++) {
                //Check if the flag of isIs_active is marked to true,
                //then activate all videos
                if (list.get(i).isIs_active()) {
                    //update all videos for a course fetched in the API as Activated
                    environment.getDatabase().updateVideosActivatedForCourse(list.get(i).getCourse().getId(),
                            dataCallback);
                } else {
                    list.remove(i);
                }
            }

            //Delete all videos which are marked as Deactivated in the database
            environment.getStorage().deleteAllUnenrolledVideos();
        }
    }

    private DataCallback<Integer> dataCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };
}
