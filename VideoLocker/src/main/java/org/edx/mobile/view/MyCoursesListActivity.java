package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.notification.NotificationDelegate;

import java.util.ArrayList;

public class MyCoursesListActivity extends BaseSingleFragmentActivity {

    @Inject
    NotificationDelegate notificationDelegate;


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

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
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

    @Override
    protected void reloadMyCoursesData() {
        CourseListTabFragment fragment = (CourseListTabFragment) getSupportFragmentManager().findFragmentByTag(FIRST_FRAG_TAG);
        if (fragment != null) {
            fragment.loadData(false, true);
        }
    }
}
