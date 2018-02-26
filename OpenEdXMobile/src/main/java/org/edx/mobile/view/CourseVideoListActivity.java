package org.edx.mobile.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.NetworkUtil;

import javax.inject.Inject;

/**
 * Created by hanning on 5/15/15.
 *
 *  @deprecated As of release v2.13, see {@link NewCourseOutlineActivity} and
 *  {@link NewCourseOutlineFragment} as an alternate.
 */
@Deprecated
public abstract class CourseVideoListActivity  extends CourseBaseActivity implements
    LastAccessManager.LastAccessManagerCallback ,VideoDownloadHelper.DownloadManagerCallback {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private boolean isFetchingLastAccessed;
    private Handler mHandler = new Handler();

    @Inject
    LastAccessManager lastAccessManager;


    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }




    @Override
    public boolean isFetchingLastAccessed() {
        return isFetchingLastAccessed;
    }

    @Override
    public void setFetchingLastAccessed(boolean accessed) {
        this.isFetchingLastAccessed = accessed;
    }

    private long lastClickTime;
    @Override
    public void showLastAccessedView(final String lastAccessedSubSectionId, final String courseId, final View view) {
        if (  isActivityStarted() ) {
            if (NetworkUtil.isConnected(this)) {
                if(courseId!=null && lastAccessedSubSectionId!=null){
                    CourseComponent lastAccessComponent = courseManager.getComponentById(courseId, lastAccessedSubSectionId);
                    if (lastAccessComponent != null) {
                        if (!lastAccessComponent.isContainer()) {   // true means its a course unit
                            // getting subsection
                            if (lastAccessComponent.getParent() != null)
                                lastAccessComponent = lastAccessComponent.getParent();
                            // now getting section
                            if (lastAccessComponent.getParent() != null) {
                                lastAccessComponent = lastAccessComponent.getParent();
                            }
                        }

                        // Handling the border case that if the Last Accessed component turns out
                        // to be the course root component itself, then we don't need to show it
                        if (!lastAccessComponent.getId().equals(courseId)) {
                            final CourseComponent finalLastAccessComponent = lastAccessComponent;
                            super.showLastAccessedView(null, lastAccessComponent.getDisplayName(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //This has been used so that if user clicks continuously on the screen,
                                    //two activities should not be opened
                                    long currentTime = SystemClock.elapsedRealtime();
                                    if (currentTime - lastClickTime > 1000) {
                                        lastClickTime = currentTime;
                                        environment.getRouter().showCourseContainerOutline(
                                                CourseVideoListActivity.this, courseData, finalLastAccessComponent.getId());
                                    }
                                }
                            });
                        }
                        else {
                            hideLastAccessedView(view);
                        }
                    } else {
                        hideLastAccessedView(view);
                    }
                }
            } else {
                hideLastAccessedView(view);
            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void showProgressDialog(int numDownloads) {}

    @Override
    public void onDownloadStarted(Long result) {
        updateListUI();
    }

    @Override
    public void onDownloadFailedToStart() {
        updateListUI();
    }

    @Override
    public abstract  void updateListUI();

}


