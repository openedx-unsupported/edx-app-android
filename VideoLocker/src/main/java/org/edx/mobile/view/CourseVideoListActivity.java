package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.DownloadManager;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtil;

/**
 * Created by hanning on 5/15/15.
 */
public abstract class CourseVideoListActivity  extends CourseBaseActivity implements
    LastAccessManager.LastAccessManagerCallback ,DownloadManager.DownloadManagerCallback {

    protected Logger logger = new Logger(getClass().getSimpleName());
    private final String modeVideoOnly = "mode_video_only";

    private boolean isFetchingLastAccessed;
    private Handler mHideHandler = new Handler();

    protected boolean videoOnlyMode = false;


    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        this.videoOnlyMode = new PrefManager.UserPrefManager(this).isUserPrefVideoModel();
    }


    public void onResume(){
        super.onResume();

        if ( courseData != null && courseData.getCourse() != null ){
            setTitle( courseData.getCourse().getName() );
            LastAccessManager.getSharedInstance().fetchLastAccessed(this, courseData.getCourse().getId());
        }

        PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(CourseVideoListActivity.this);
        boolean currentVideoMode = userPrefManager.isUserPrefVideoModel();
        if ( currentVideoMode != videoOnlyMode ){
            updateListUI();
        }
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
    public void showLastAccessedView(String lastAccessedSubSectionId, final String courseId, final View view) {
        if (  isActivityStarted() ) {
            if (!AppConstants.offline_flag) {
                try {
                    if(courseId!=null && lastAccessedSubSectionId!=null){
                        final Api api = new Api(this);
                        final VideoResponseModel videoModel = api.getSubsectionById(courseId,
                            lastAccessedSubSectionId);
                        if (videoModel != null) {
                            super.showLastAccessedView(null, " " + videoModel.getSection().name, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //This has been used so that if user clicks continuously on the screen,
                                    //two activities should not be opened
                                    long currentTime = SystemClock.elapsedRealtime();
                                    if (currentTime - lastClickTime > 1000) {
                                        lastClickTime = currentTime;
                                        try {
                                            LectureModel lecture = api.getLecture(courseId,
                                                videoModel.getChapterName(),
                                                videoModel.getSequentialName());
                                            SectionEntry chapter = new SectionEntry();
                                            chapter.chapter = videoModel.getChapterName();
                                            lecture.chapter = chapter;
                                            Intent videoIntent = new Intent(
                                                CourseVideoListActivity.this,
                                                VideoListActivity.class);
                                            videoIntent.putExtra(Router.EXTRA_ENROLLMENT, courseData);
                                            videoIntent.putExtra("lecture", lecture);
                                            videoIntent.putExtra("FromMyVideos", false);

                                            startActivity(videoIntent);
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                    }
                                }
                            });
                        } else {
                            hideLastAccessedView(view);
                        }
                    }
                } catch (Exception e) {
                    hideLastAccessedView(view);
                    logger.error(e);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_change_mode:
                changeMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void changeMode(){
        //Creating the instance of PopupMenu
        org.edx.mobile.view.custom.popup.menu.PopupMenu popup = new org.edx.mobile.view.custom.popup.menu.PopupMenu(this,
            findViewById(R.id.action_change_mode), Gravity.START);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
            .inflate(R.menu.change_mode, popup.getMenu());
        final PrefManager.UserPrefManager userPrefManager =
            new PrefManager.UserPrefManager(this);
        final MenuItem videoOnlyItem = popup.getMenu().findItem(R.id.change_mode_video_only);
        MenuItem fullCourseItem = popup.getMenu().findItem(R.id.change_mode_full_mode);
        // Initializing the font awesome icons
        IconDrawable videoOnlyIcon = new IconDrawable(this, Iconify.IconValue.fa_film);
        IconDrawable fullCourseIcon = new IconDrawable(this, Iconify.IconValue.fa_list);
        videoOnlyItem.setIcon(videoOnlyIcon);
        fullCourseItem.setIcon(fullCourseIcon);
        // Setting checked states
        if (userPrefManager.isUserPrefVideoModel()) {
            videoOnlyItem.setChecked(true);
            videoOnlyIcon.colorRes(R.color.cyan_4);
            fullCourseIcon.colorRes(R.color.black);
        } else {
            fullCourseItem.setChecked(true);
            fullCourseIcon.colorRes(R.color.cyan_4);
            videoOnlyIcon.colorRes(R.color.black);
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new org.edx.mobile.view.custom.popup.menu.PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                PrefManager.UserPrefManager userPrefManager =
                    new PrefManager.UserPrefManager(CourseVideoListActivity.this);
                boolean currentVideoMode = userPrefManager.isUserPrefVideoModel();
                boolean selectedVideoMode = videoOnlyItem == item;
                if ( currentVideoMode == selectedVideoMode )
                    return true;

                userPrefManager.setUserPrefVideoModel(selectedVideoMode);
                updateListUI();
                invalidateOptionsMenu();
                return true;
            }
        });

        popup.show(); //showing popup menu

    }

    @Override
    protected void updateDownloadProgress(int progressPercent){
        if ( progressPercent == 0 ) {
            setVisibilityForDownloadProgressView(false);
        } else if ( progressPercent < 100 ){
            setVisibilityForDownloadProgressView(true);
            mHideHandler.removeCallbacks(mHideRunnable);
            downloadIndicator.setVisibility(View.VISIBLE);
            downloadIndicator.setRotation( progressPercent * 360 /100 );
        } else { //progressPercent == 100
            downloadIndicator.setVisibility(View.INVISIBLE);
            mHideHandler.postDelayed(mHideRunnable,
                getResources().getInteger(R.integer.message_delay));
        }
    }

    @Override
    public void onDownloadSuccess(Long result) {
        try {
            updateListUI();
            //TODO - ideally it should merge to message view into one.
            String content = ResourceUtil.getFormattedStringForQuantity(R.plurals.downloading_count_videos, result.intValue()).toString();
            UiUtil.showMessage(findViewById(R.id.drawer_layout), content);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onDownloadFailure() {
    }

    @Override
    public void showProgressDialog() {
    }

    @Override
    public abstract  void updateListUI();


    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            setVisibilityForDownloadProgressView(false);
        }
    };
}


